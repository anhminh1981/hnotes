package dao

import javax.inject.Inject
import scala.concurrent.Future

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import models.Note
import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import org.joda.time.Instant


class NoteDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]{
  import driver.api._
  
  implicit def dateTime  =
        MappedColumnType.base[DateTime, Timestamp](
          dt => new Timestamp(dt.getMillis),
          ts => new DateTime(ts.getTime)
    )
  
  private val Notes = TableQuery[NotesTable]
  
  def allFromUser(ownerId: Long, order: NoteDao.NoteOrder = NoteDao.lastModified): Future[Seq[Note]] = { 
    val query = Notes.filter(_.owner === ownerId)
    val queryWithOrder = order match {
      case NoteDao.lastModified => query.sortBy { _.modifiedAt.desc }
      case NoteDao.titleDesc => query.sortBy { _.title.desc.nullsFirst }
      case NoteDao.titleAsc => query.sortBy { _.title.asc.nullsLast }
    }
    val mappedQuery = queryWithOrder.map(note => (note.id, note.owner, note.typeNote, note.title, note.text, note.createdAt, note.modifiedAt))
    db.run(mappedQuery.result) map { _ map { case (id, owner, typeNote, title, text, createdAt, modifiedAt) => Note(id, owner, typeNote, title, text, null, createdAt, modifiedAt) } }
  }
  
  def getById(id: Long): Future[Option[Note]] = {
    val query = Notes.filter (_.id === id )
    db.run(query.result ) map { seq => if(seq.isEmpty) None else Some(seq.head)}
  }
  
  def update(id: Long, title: String, text: String): Future[Int] = {
    val query = for(n <- Notes if n.id === id) yield (n.title, n.text, n.modifiedAt)
    val updateAction = query.update((title, text, Instant.now().toDateTime()))
    db.run(updateAction)
  }
  
  def insert(note: Note): Future[Long] = {
    db.run((Notes returning Notes.map(_.id)) += note)
  }
  
  private class NotesTable(tag: Tag) extends Table[Note](tag, "NOTES") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def owner = column[Long]("OWNER")
    def typeNote = column[String]("TYPE")
    def title = column[String]("TITLE")
    def text = column[String]("TEXT")
    def data = column[Array[Byte]]("DATA")
    def createdAt = column[DateTime]("CREATEDAT")
    def modifiedAt = column[DateTime]("MODIFIEDAT")
    
    def * = (id, owner, typeNote, title, text, data, createdAt, modifiedAt) <> (Note.tupled, Note.unapply _)
    
  }
  
}

object NoteDao {
  sealed trait NoteOrder
  final object titleAsc extends NoteOrder
  final object titleDesc extends NoteOrder
  final object lastModified extends NoteOrder
  
}