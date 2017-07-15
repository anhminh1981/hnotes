package controllers

import javax.inject.Inject
import dao.NoteDao
import scala.concurrent.{ ExecutionContext, Future, Promise }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller
import play.api.Configuration
import play.api.mvc.Action
import scala.concurrent.Future
import play.api.mvc.BodyParsers.parse
import play.api.libs.json.JsValue
import play.api.mvc.Request
import play.api.libs.json._
import models.Note
import java.util.Base64
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc.WrappedRequest
import models.User
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc.ActionRefiner
import play.api.mvc.ActionFilter
import play.api.mvc.Result
import play.api.Logger
import play.api.Environment

class NoteController @Inject() (noteDao: NoteDao)(implicit val configuration: Configuration, implicit val env: Environment)
    extends Controller with Secured {
  val summaryLength = 20
  val updateSuccess = Json.obj("request" -> "update", "status" -> "OK")
  val noteWrites = new Writes[Note] {
    def writes(note: Note) = {
      val encodedData = Base64.getEncoder.encode(note.data)
      Json.obj(
        "id" -> note.id,
        "owner" -> note.owner,
        "type" -> note.typeNote,
        "title" -> note.title,
        "text" -> note.text,
        "data" -> encodedData,
        "createdAt" -> note.createdAt.getMillis,
        "modifiedAt" -> note.modifiedAt.getMillis)
    }
  }

  val summaryNoteWrites = new Writes[Note] {
    def writes(note: Note) = {
      Json.obj(
        "id" -> note.id,
        "owner" -> note.owner,
        "type" -> note.typeNote,
        "title" -> (if (note.title.length < summaryLength) note.title else note.title.substring(0, summaryLength) + "..."),
        "text" -> (if (note.text.length < summaryLength) note.text else note.text.substring(0, summaryLength) + "..."),
        "createdAt" -> note.createdAt.getMillis,
        "modifiedAt" -> note.modifiedAt.getMillis)
    }
  }

  def notes = Authenticated.async { implicit request =>
    implicit val writes = summaryNoteWrites
    val notes = noteDao.allFromUser(request.user.id);
    notes map {
      Json.toJson(_)
    } map (jsNotes => {
      Logger.debug(jsNotes.toString())
      Ok(Json.obj("notes" -> jsNotes))
    })
  }

  class ItemRequest[A](val item: Note, request: UserRequest[A]) extends WrappedRequest[A](request) {
    def user = request.user
  }

  def ItemAction(itemId: Long) = new ActionRefiner[UserRequest, ItemRequest] {
    def refine[A](request: UserRequest[A]) = {
      noteDao.getById(itemId).map {
        _ map { new ItemRequest(_, request) } toRight (NotFound)
      }
    }
  }

  object PermissionCheckAction extends ActionFilter[ItemRequest] {
    def filter[A](request: ItemRequest[A]) = Future.successful {
      if (request.item.owner != request.user.id)
        Some(Forbidden)
      else
        None
    }
  }
  def note(noteId: Long) = (Authenticated andThen ItemAction(noteId) andThen PermissionCheckAction) { implicit request =>
    Ok(Json.toJson(request.item)(noteWrites))
  }

  object NotePostAction extends ActionRefiner[UserRequest, ItemRequest] {
    def refine[A](request: UserRequest[A]): Future[Either[Result, ItemRequest[A]]] = {
      if (request.body.isInstanceOf[JsValue]) {
        val body = request.body.asInstanceOf[JsValue]

        Logger.debug("body: " + body.toString())
        val jsLookup = body \ "id"
        Logger.debug("body \\ id: " + jsLookup.toString())
        val itemId = jsLookup.asOpt[Long]

        Logger.debug(s"itemId = $itemId")

        if (itemId == None) {
          Future.successful(Left(BadRequest("Bad format")))
        } else {
          noteDao.getById(itemId.get).map {
            _ map { new ItemRequest(_, request) } toRight (NotFound)
          }
        }
      } else {
        throw new IllegalStateException
      }
    }
  }

  def edit = (Authenticated andThen NotePostAction andThen PermissionCheckAction).async(parse.json) { implicit request =>
    val body = request.body

    val oldNote = request.item // uselessfor now
    val id = oldNote.id
    val title = (body \ "title").asOpt[String]
    val text = (body \ "text").asOpt[String]
    noteDao.update(id, title.getOrElse(""), text.getOrElse("")) map { _ => Ok(updateSuccess + ("id" -> JsNumber(id))) }
  }
}