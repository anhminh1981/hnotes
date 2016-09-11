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
import com.google.inject.ImplementedBy


@Singleton
class AuthController @Inject() (userDao: UserDao, configuration: play.api.Configuration, secured: Secured) (implicit ec: ExecutionContext) extends Controller  {
  val signupSuccess = Json.obj("request" -> "signup", "status" -> "OK")
  val signupFailure = Json.obj("request" -> "signup", "status" -> "KO")
  val loginSuccess = Json.obj("request" -> "login", "status" -> "OK")
  val loginFailure = Json.obj("request" -> "login", "status" -> "KO")
    
  // Lazy so the mock can be told what to do after instanciation in the tests
  lazy val secretOption = configuration.getString("play.crypto.secret")
  
  
  implicit val userWrites: Writes[User] = ( 
    (JsPath \ "email").write[String] and (JsPath \ "role").write[String]
  )(user => (user.email, user.role))
  

  
  private def commonNeedPasswordAndEmail(f: (String, String) => Future[Result]) = Action.async(parse.json) { 
		implicit request => {
		  Logger.debug("common need")
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
    Logger.debug("signup")
    val existingUser = userDao.selectByEmail(email )
  
    existingUser flatMap { user => 
    if(user.isDefined) 
  	  Future.successful(Ok(signupFailure + ("cause" -> JsString("email already registered")) )) 
  	  else {
  		  val newUser = userDao.insert(User(0, email, password.bcrypt, "user")) 
  				  newUser map { user2 => Ok(signupSuccess + ("token" -> JsString(createToken(user2))) + ("user" -> Json.toJson(user2)))}
  	  }
    }

  }

    
  def createToken(user: User) = { 
	  secretOption match { 
	  case Some(secret) => val now = Instant.now()

			  val claim = Json.obj("iat" -> now.getMillis , "iss" -> "hnotes", "email" -> user.email, "role" -> user.role)

			  
			  JwtJson.encode(claim, secret, AuthConstants.algo)
	  case None => throw new IllegalStateException("The application must have a secret")
	  }
  }
  
  
  def login = commonNeedPasswordAndEmail { (email, password) => 
    Logger.debug("login")
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
  
  def test = secured.secure { implicit request =>
    Logger.debug(request.user.toString())
    Ok("ok")
  }
  
}

object AuthConstants { 
  val algo = JwtAlgorithm.HS256
  val duration =  365 * 3600 * 1000L // 365 jours en millis
}


class UserRequest[A](val user: Option[User], request: Request[A]) extends WrappedRequest[A](request)



class Secured @Inject() ( configuration: play.api.Configuration) { 
	val secretOption = configuration.getString("play.crypto.secret")
	object UserAction extends
			ActionBuilder[UserRequest] with ActionTransformer[Request, UserRequest] {

		def transform[A](request: Request[A]) = Future.successful {
			val user = for(token <- request.headers.get("authorization");
					secret <- secretOption;
					claim <- JwtJson.decodeJson(token, secret, Seq(AuthConstants.algo)).toOption;
					iat <- (claim \ "iat").asOpt[Long] if  Instant.now().getMillis < iat + AuthConstants.duration ) 
				yield User(0, (claim \ "email").as[String], null, (claim \ "role").as[String])


				new UserRequest(user, request)
		}
	}

	object PermissionCheckAction extends ActionFilter[UserRequest] {
		def filter[A](request: UserRequest[A]) = Future.successful {
			if (request.user.isEmpty)
				Some(Results.Unauthorized)
		  else
				None
		}
	}

	def secure = UserAction andThen PermissionCheckAction
}
