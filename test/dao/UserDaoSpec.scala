package dao

import scala.concurrent.ExecutionContext.Implicits._

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.Headers
import play.api.libs.json.Json
import play.api.db.slick.DatabaseConfigProvider
import slick.profile.BasicProfile
import models.User
import org.mockito.internal.matchers.GreaterThan
import com.github.t3hnar.bcrypt.Password
import scala.concurrent.Await
import scala.concurrent.duration._

class UserDaoSpec extends PlaySpec with OneAppPerSuite {
  object dbConfigProvider extends DatabaseConfigProvider {
    def get[P <: BasicProfile] = DatabaseConfigProvider.get
  }
  
  "UserDao" should {
    "insert a user" in {
      val userDao = new UserDao(dbConfigProvider)
      val newUser = userDao.insert(User(0, "a@a.a", "a", "user"))
      newUser.map { user =>
        user.isSuccess must be 
    	  user.get.id must be > 0L
    	  "a".isBcrypted( user.get.password )
      }
    }
  }
  
    "not insert a user twice" in {
      val userDao = new UserDao(dbConfigProvider)
      val newUser = userDao.insert(User(0, "a@a.a", "a", "user"))
      newUser.map { user => 
        user.isFailure must be
      } 
    }
  
}