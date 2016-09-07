package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.mvc._
import play.api.mvc.BodyParsers.parse
import scala.concurrent.{ExecutionContext, Future, Promise}
import dao.UserDao
import models.User

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Calendar
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtJson
import org.joda.time.DateTime
import org.joda.time.Instant
import com.github.t3hnar.bcrypt._


@Singleton
class AuthController @Inject() (userDao: UserDao, configuration: play.api.Configuration) (implicit ec: ExecutionContext) extends Controller {
  val signupSuccess = Json.obj("request" -> "signup", "status" -> "OK")
  val signupFailure = Json.obj("request" -> "signup", "status" -> "KO")
  val loginSuccess = Json.obj("request" -> "login", "status" -> "OK")
  val loginFailure = Json.obj("request" -> "login", "status" -> "KO")
  
  implicit val userWrites: Writes[User] = ( 
    (JsPath \ "email").write[String] and (JsPath \ "role").write[String]
  )(user => (user.email, user.role))
  
  val secretOption = configuration.getString("play.crypto.secret")
  
  private def commonNeedPasswordAndEmail(f: (String, String) => Future[Result]) = Action.async(parse.json) { 
		implicit request => {
		  Logger.debug("signup")
			val email = (request.body \ "email").asOpt[String]
			val password = (request.body \ "password").asOpt[String]

			if(email == None || password == None || email.get.isEmpty() || password.get.isEmpty()) {
				Future.successful(BadRequest("needs both email and password"))
			} else { 
				f(email.get, password.get)
			}
		}
  }
  
  
  def signup = commonNeedPasswordAndEmail { (email, password) => 
    val futureResult = for(
		  existingUser <- userDao.selectByEmail(email ) if existingUser.isEmpty;
		  newUser <- userDao.insert(User(0, email, password.bcrypt, "user")) 
		  )  yield Ok(signupSuccess + ("token" -> JsString(createToken(newUser))) + ("user" -> Json.toJson(newUser)))
		  futureResult fallbackTo(Future.successful(Ok(signupFailure + ("cause" -> JsString("email already registered")))))
  }

    
  def createToken(user: User) = { 
	  secretOption match { 
	  case Some(secret) => val now = Instant.now()

			  val claim = Json.obj("iat" -> now.getMillis, "iss" -> "hnotes", "email" -> user.email, "role" -> user.role)

			  val algo = JwtAlgorithm.HS256
			  JwtJson.encode(claim, secret, algo)
	  case None => throw new IllegalStateException("The application must have a secret")
	  }
  }
  
  
  def login = commonNeedPasswordAndEmail { (email, password) => 
    val userFuture = userDao.selectByEmail(email )
    userFuture map { 
      case None => Ok(loginFailure + ("cause" -> JsString("email not found")))
      case Some(user) => 
        if(password.isBcrypted(user.password)) {
          val userJson = Json.toJson(user)
        	Ok(loginSuccess + ("token" -> JsString(createToken(user))) + ("user" -> userJson)) 
        } else
        	Ok(loginFailure + ("cause" -> JsString("wrong password")))
      
    }
  }

}