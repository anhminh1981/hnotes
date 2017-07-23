package services

import javax.inject.Singleton
import javax.inject.Inject
import dao.UserDao
import dao.NoteDao
import models.User
import models.Note

import scala.util.Success
import scala.util.Failure
import play.api.Logger
import org.joda.time.Instant
import scala.concurrent.ExecutionContext

@Singleton
class DevData @Inject() (userDao: UserDao, noteDao: NoteDao)(implicit val ec: ExecutionContext) {
  val now = Instant.now().toDateTime()
  val text = """
    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas convallis magna ullamcorper, semper orci sit amet, 
    commodo quam. Ut ornare, dolor tincidunt mollis commodo, lacus est vehicula elit, non dictum nisl elit sit amet erat. 
    Ut felis turpis, fermentum sodales pellentesque ut, efficitur at elit. Suspendisse potenti. Phasellus eu eleifend augue. 
    Donec faucibus urna non mauris efficitur tincidunt. Donec hendrerit nec sem nec mollis. Aenean ac sodales leo. Vestibulum 
    at cursus tortor. Nullam eget ante nec massa tincidunt porta. Praesent eget convallis enim, et tempus orci. Aenean ornare 
    pellentesque odio vel tincidunt. Pellentesque tortor ligula, rhoncus a pharetra et, pharetra eu augue. Suspendisse potenti. 
    Nam augue lacus, maximus in volutpat eu, auctor id mi. Vivamus commodo mauris turpis, in consectetur lorem imperdiet nec. 
    """
  val result = for (
    existing <- userDao.selectByEmail("test@test.test") if existing.isEmpty;
    user <- userDao.insert(User(0, "test@test.test", "test", "user"));
    note <- noteDao.insert(Note(0, user.get.id, "text", "title", text, Array[Byte](), now, now));
    note2 <- noteDao.insert(Note(0, user.get.id, "text", "title2", "azerty", Array[Byte](), now, now))
  ) yield note2

  result.onComplete {
    case Success(n) => {
      Logger.debug(s"Success creating note $n")
    }
    case Failure(t) => Logger.error("Error inserting dev data " + t.toString())
  }
}