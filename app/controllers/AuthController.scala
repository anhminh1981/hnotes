package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.mvc._
import play.api.mvc.BodyParsers.parse
import scala.concurrent.{ExecutionContext, Future, Promise}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
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
import play.api.libs.ws.WSClient


@Singleton
class AuthController @Inject() (userDao: UserDao)  (implicit val configuration: Configuration, env: Environment, ws: WSClient) extends Controller with Secured {
  val signupSuccess = Json.obj("request" -> "signup", "status" -> "OK")
  val signupFailure = Json.obj("request" -> "signup", "status" -> "KO")
  val loginSuccess = Json.obj("request" -> "login", "status" -> "OK")
  val loginFailure = Json.obj("request" -> "login", "status" -> "KO")
    
  // Lazy so the mock can be told what to do after instanciation in the tests
  lazy val secretOption = configuration.getString("play.crypto.secret")
  
  val auth0UrlOption = configuration.getString("auth0.domain") map {"https://" + _}
  
  
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
    Logger.debug("password " + password + " " + """^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{6,20}$""".r.pattern.matcher(password).find())
    if(Constraints.emailAddress(email).isInstanceOf[Invalid] ) {
      Future.successful(Ok(signupFailure + ("cause" -> JsString("wrong email format")) ))
    } else if((env.mode != Mode.Dev || configuration.getBoolean("dev.check.password").getOrElse(false)) 
        && Constraints.pattern("""^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{6,20}$""".r)(password).isInstanceOf[Invalid]) {
      Future.successful(Ok(signupFailure + ("cause" -> JsString("password must be 6 to 20 characters long, and must contain lower case, upper case, digit and special characters")) ))
    } else {
      val existingUser = userDao.selectByEmail(email )
  
      existingUser flatMap { user => 
      if(user.isDefined) 
    	  Future.successful(Ok(signupFailure + ("cause" -> JsString("email already registered")) )) 
    	  else {
    		  val newUser = userDao.insert(User(0, email, password, "user"))
    		  Logger.debug("new user: " + newUser)
    		  newUser map { user2 => Ok(signupSuccess + ("token" -> JsString(createToken(user2))) + ("user" -> Json.toJson(user2)))}
    	  }
      }
    }
    

  }

    
  def auth0Login = Action.async(parse.json) { implicit request =>
    Logger.debug("auth0 login")
    val tokenOption = (request.body \ "token").asOpt[String]
	  (auth0UrlOption, tokenOption) match {
      case (None, _) => Future.successful(Ok(loginFailure + ("cause" -> JsString("auth0 not configured"))) )
	    case (_, None) => Future.successful(Ok(loginFailure + ("cause" -> JsString("no token"))))
	    case (Some(url), Some(token)) => {
	      val request = ws.url(url + "/tokeninfo").withHeaders("Accept" -> "application/json", "Content-Type" -> "application/json")
  		  val body = Json.obj("id_token" -> token)
  		  val res = for(response <- request.post(body) if (response.json \ "email").toOption.isDefined && (response.json \ "email_verified").asOpt[Boolean] == Some(true);
  				  userOpt <- userDao.selectByEmail((response.json \ "email").as[String]) 
  				  ) yield userOpt match {
  		      case None => Ok(loginFailure + ("cause" -> JsString("email not found")))
  				  case Some(user) => Ok(loginSuccess + ("token" -> JsString(createToken(user))) + ("user" -> Json.toJson(userOpt)))
  		  }

        res fallbackTo(Future.successful(Ok(loginFailure+ ("cause" -> JsString("invalid auth0 token")))))
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
  
  def test = Authenticated { implicit request =>
    Logger.debug(request.user.toString())
    Ok("ok")
  }
  
}

object AuthConstants { 
  val algo = JwtAlgorithm.HS256
  val duration =  365 * 3600 * 1000L // 365 jours en millis
}





trait Secured  {
  implicit val configuration: Configuration
  
	private lazy val secretOption = configuration.getString("play.crypto.secret")
	
	
	
	def getUserFromRequest[A](requestHeader: RequestHeader) = {
		for(token <- requestHeader.headers.get("authorization");
				secret <- secretOption;
				claim <- JwtJson.decodeJson(token, secret, Seq(AuthConstants.algo)).toOption if(claim \ "iss").asOpt[String] == Some("hnotes");
				iat <- (claim \ "iat").asOpt[Long] if  Instant.now().getMillis < iat + AuthConstants.duration ) 
			yield User((claim \ "userId").as[Long], null, null, (claim \ "role").as[String])
	}
  
	object Authenticated extends AuthenticatedBuilder(getUserFromRequest, _ => Results.Unauthorized)
	
	type UserRequest[A] = AuthenticatedRequest[A, User]
}
