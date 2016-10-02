import slick.profile.BasicProfile
import play.api.db.slick.DatabaseConfigProvider
import play.api.Application
import dao.UserDao
import dao.NoteDao
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models._
import scala.util.Success
import scala.util.Failure
import play.api.Logger
import org.joda.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration._


trait TestData {
  implicit val app: Application
  object dbConfigProvider extends DatabaseConfigProvider {
    def get[P <: BasicProfile] = DatabaseConfigProvider.get
  }
  val userDao = new UserDao(dbConfigProvider)
  val noteDao = new NoteDao(dbConfigProvider)
  private val now = Instant.now().toDateTime()
  
  var noteId1: Long = 0
  var noteId2: Long = 0
  var noteId3: Long = 0
  
  def insertData = {
	  val result = for(
			  existing <- userDao.selectByEmail("test@test.test") if existing.isEmpty;
			  user <- userDao.insert(User(0, "test@test.test", "Test_123", "user"));
			  user2 <- userDao.insert(User(0, "test2@test.test", "Test_1223", "user"));
			  note <- noteDao.insert(Note(0, user.id, "text", "title", "lore ipsum", Array[Byte](), now, now) ) ;
			  note2 <- noteDao.insert(Note(0, user.id, "text", "title2", "lore ipsum", Array[Byte](), now, now) ) ;
			  note3 <- noteDao.insert(Note(0, user2.id, "text", "title3", "lore ipsum", Array[Byte](), now, now) ) 
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