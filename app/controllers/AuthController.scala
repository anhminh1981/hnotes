package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future, Promise}
import dao.UserDao
import models.User

import play.api.Configuration
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.util.Calendar
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtJson
import org.joda.time.DateTime
import org.joda.time.Instant
import com.github.t3hnar.bcrypt._
import com.google.inject.ImplementedBy
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.RequestHeader
import play.api.mvc.Security.AuthenticatedRequest
import play.api.data.validation.Constraints
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import scala.util.Success
import scala.util.Failure
import dao.exception.InsertDuplicateException
import play.api.data.validation.Constraint
import views.html.defaultpages.unauthorized
import akka.stream.ActorMaterializer
import akka.stream.Materializer


@Singleton
class AuthController @Inject() (userDao: UserDao)  (implicit val configuration: Configuration, val env: Environment, val ec: ExecutionContext, val mat: Materializer) extends  Secured {
  val signupSuccess = Json.obj("request" -> "signup", "status" -> "OK")
  val signupFailure = Json.obj("request" -> "signup", "status" -> "KO")
  val loginSuccess = Json.obj("request" -> "login", "status" -> "OK")
  val loginFailure = Json.obj("request" -> "login", "status" -> "KO")
    
  // Lazy so the mock can be told what to do after instanciation in the tests
  lazy val secretOption = configuration.getOptional[String]("play.crypto.secret")
  
  
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
  
  def createToken(user: User) = { 
	  secretOption match { 
	  case Some(secret) => val now = Instant.now()

			  val claim = Json.obj("iat" -> now.getMillis , "iss" -> "hnotes", "userId" -> user.id, "role" -> user.role)

			  
			  JwtJson.encode(claim, secret, AuthConstants.algo)
	  case None => throw new IllegalStateException("The application must have a secret")
	  }
  }

  def signup = commonNeedPasswordAndEmail { (email, password) => 
    Logger.debug("signup")
    val emailVal: Constraint[String] = Constraints.emailAddress
    if(emailVal(email).isInstanceOf[Invalid] ) {
      Future.successful(Ok(signupFailure + ("cause" -> JsString("wrong email format")) ))
    } else if((env.mode != Mode.Dev || configuration.getOptional[Boolean]("dev.check.password").getOrElse(false)) 
        && Constraints.pattern("""^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{6,20}$""".r)(password).isInstanceOf[Invalid]) {
      Future.successful(Ok(signupFailure + ("cause" -> JsString("password must be 6 to 20 characters long, and must contain lower case, upper case, digit and special characters")) ))
    } else {
    		  val newUser = userDao.insert(User(0, email, password, "user"))
    		  newUser map { _ match {
    		    case Success(user2) => Ok(signupSuccess  + ("user" -> (Json.toJson(user2).as[JsObject] + ("token" -> JsString(createToken(user2)))    )))
    		    case Failure(InsertDuplicateException(_, _, _)) => Ok(signupFailure + ("cause" -> JsString("email already registered")))
    		    case Failure(e) => InternalServerError(signupFailure)
    		  } 
    		  }

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
  
  def test = authenticated { implicit request =>
    Logger.debug(request.user.toString())
    Ok("ok")
  }
  
  
}

object AuthConstants { 
  val algo = JwtAlgorithm.HS256
  val duration =  365 * 3600 * 1000L // 365 jours en millis
  val authPrefix = "Bearer "
}





trait Secured extends InjectedController {
  val configuration: Configuration
  val env: Environment
  implicit val ec: ExecutionContext
	implicit val mat: Materializer
	
	
	def getUserFromRequest[A](requestHeader: RequestHeader) = {
    if(env.mode == Mode.Dev && configuration.getOptional[Boolean]("dev.auth.skip").getOrElse(false)) {
      Some(User(1, "test@test.test", null, "user"))
    } else {
    	for(authorization <- requestHeader.headers.get("authorization") if authorization startsWith AuthConstants.authPrefix;
    			secret <- configuration.getOptional[String]("play.crypto.secret");
    			claim <- JwtJson.decodeJson(authorization.substring(AuthConstants.authPrefix.length()).trim(), 
    					secret, Seq(AuthConstants.algo)).toOption if(claim \ "iss").asOpt[String] == Some("hnotes");
    			iat <- (claim \ "iat").asOpt[Long] if  Instant.now().getMillis < iat + AuthConstants.duration ) 
    		yield User((claim \ "userId").as[Long], null, null, (claim \ "role").as[String])
    }
      
	}
  
	class Authenticated(implicit val ec: ExecutionContext, implicit val mat: Materializer) 
	  extends AuthenticatedBuilder[User](getUserFromRequest, new BodyParsers.Default)(ec) {
  }
  
	def authenticated = new Authenticated
	
	type UserRequest[A] = AuthenticatedRequest[A, User]
}
