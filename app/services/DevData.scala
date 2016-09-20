package services

import javax.inject.Singleton
import javax.inject.Inject
import dao.UserDao
import dao.NoteDao
import models.User
import models.Note

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.util.Success
import scala.util.Failure
import play.api.Logger
import org.joda.time.Instant


@Singleton
class DevData @Inject() (userDao: UserDao, noteDao: NoteDao) {
  val now = Instant.now().toDateTime()
  val result = for(
      existing <- userDao.selectByEmail("test@test.test") if existing.isEmpty;
      user <- userDao.insert(User(0, "test@test.test", "test", "user"));
      note <- noteDao.insert(Note(0, user.id, "text", "title", "lore ipsum", Array[Byte](), now, now) ) ) yield note
  
  result.onComplete {
    case Success(n) => {
      Logger.debug(s"Success creating note $n" )
    }
    case Failure(t) => Logger.error("Error inserting dev data " + t.toString())
  }
}