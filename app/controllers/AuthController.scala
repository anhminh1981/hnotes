package controllers

import javax.inject.{Inject, Singleton}
import play.api._
import play.api.mvc._
import play.api.mvc.BodyParsers.parse
import scala.concurrent.{ExecutionContext, Future, Promise}
import dao.UserDao
import models.User
import play.api.libs.json.Json


@Singleton
class AuthController @Inject() (userDao: UserDao) (implicit ec: ExecutionContext) extends Controller {
  val signupSuccess = Json.obj("request" -> "signup", "status" -> "OK")
  val signupFailure = Json.obj("request" -> "signup", "status" -> "KO", "cause" -> "email already registered")
  
	def signup = Action.async(parse.json) { 
		implicit request => {
		  Logger.debug("signup")
			val email = (request.body \ "email").asOpt[String]
			val password = (request.body \ "password").asOpt[String]

			if(email == None || password == None) {
				Future.successful(BadRequest("needs both email and password"))
			} else { 
				userDao.selectByEmail(email.get ) filter ( _.isEmpty) map { _ =>
				  userDao.insert(User(0, email.get, password.get, "user"))
				} map {_ => Ok(signupSuccess)} fallbackTo(Future.successful(Ok(signupFailure)))
				
				
			}
		}
  }

  def createToken(user: User) = Nil
	def login = Action.async {
		Future.successful(Ok("login"))
	}

}