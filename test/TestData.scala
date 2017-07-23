import slick.basic.BasicProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.Application
import dao.UserDao
import dao.NoteDao
import models._
import scala.util.Success
import scala.util.Failure
import play.api.Logger
import org.joda.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.Play
import dao.UserDao
import scala.concurrent.ExecutionContext


trait TestData {
  implicit val app: Application
  val userDao = app.injector.instanceOf[UserDao]
  val noteDao = app.injector.instanceOf[NoteDao]
  implicit val ec = app.injector.instanceOf[ExecutionContext] 
  private val now = Instant.now().toDateTime()
  
  var noteId1: Long = 0
  var noteId2: Long = 0
  var noteId3: Long = 0
  
  def insertData = {
	  val result = for(
			  existing <- userDao.selectByEmail("test@test.test") if existing.isEmpty;
			  user <- userDao.insert(User(0, "test@test.test", "Test_123", "user"));
			  user2 <- userDao.insert(User(0, "test2@test.test", "Test_1223", "user"));
			  note <- noteDao.insert(Note(0, user.get.id, "text", "title", "lore ipsum", Array[Byte](), now, now) ) ;
			  note2 <- noteDao.insert(Note(0, user.get.id, "text", "title2", "lore ipsum", Array[Byte](), now, now) ) ;
			  note3 <- noteDao.insert(Note(0, user2.get.id, "text", "title3", "lore ipsum", Array[Byte](), now, now) ) 
			  ) yield {
	    noteId1 = note
	    noteId2 = note2
	    noteId3 = note3
	  }

	  result.onComplete {
	    case Success(_) => {
		    Logger.debug("Success creating test data" )
	  }
	    case Failure(t) => Logger.error("Error inserting test data: " + t.toString())
	  }
	  
	  Await.result(result, 1 second)

  }
}