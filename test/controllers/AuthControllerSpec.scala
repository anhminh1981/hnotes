package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import org.joda.time.DateTimeUtils
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import com.github.t3hnar.bcrypt.Password

import akka.stream.Materializer
import dao.UserDao
import dao.exception.InsertDuplicateException
import models.User
import play.api.Configuration
import play.api.Environment
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import play.api.test.Helpers.POST
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.mvc.ControllerComponents
import org.scalatestplus.play.guice.GuiceOneAppPerSuite


class AuthControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {
  // default values
  val email = "test@test.test"
  val password = "Aa123_4567"
  val role = "user"
  val user = User(2, email, password.bcrypt, role)
  //Mocking the secret
  private val secret = "changeme"
  
  private implicit val configuration = mock[Configuration]
  private implicit val env = mock[Environment]
  private implicit val mat = mock[Materializer]
  
  private val components = app.injector.instanceOf[ControllerComponents]
  
  private var userDao = null: UserDao
  private var controller = null: AuthController
  
  override def beforeEach() = { 
    when(configuration.getOptional[String]("play.crypto.secret")) thenReturn Some(secret)
    userDao = mock[UserDao]
    controller = new AuthController(userDao)
    
    controller.setControllerComponents( components )
    
  }
  
  "createToken" must { 
    "create a token if there's a secret" in { 
      
      // Mocking the time
      val provider = mock[DateTimeUtils.MillisProvider]
      when(provider.getMillis) thenReturn 1473874241026L
      DateTimeUtils.setCurrentMillisProvider(provider)
      // Expected result
      val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
      val claim = "eyJpYXQiOjE0NzM4NzQyNDEwMjYsImlzcyI6Imhub3RlcyIsInVzZXJJZCI6Miwicm9sZSI6InVzZXIifQ"
      val signature = "kHOkwcTWLOOikw4PQ0XkD7cUHbb7otiIvV_td1wTnVs"
      
      // Running the method
      val token = controller.createToken(user) 
      
      // Result
      token must equal(s"$header.$claim.$signature")
      verify(configuration, atLeastOnce()).getOptional[String]("play.crypto.secret")
    }
    
    "throw an exception if the secret's not configured" in {
      when(configuration.getOptional[String]("play.crypto.secret")) thenReturn None
      an [IllegalStateException] must be thrownBy controller.createToken(user) 
    }
  }
  
  "signup" should {
    
    "need an email" in { 
      val requestBody = Json.obj( "password" -> password )
      val request = FakeRequest(POST, "/api/signup", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      
      val result = controller.signup().apply(request)
      
      val BAD_REQUEST_CODE = 400
      result.onComplete { 
        case Success(r) => r.header.status must be (BAD_REQUEST_CODE)
        case Failure(e) => fail 
        }
    }
    
    
    "register a new user" in { 
    	when(configuration.getOptional[String]("play.crypto.secret")) thenReturn Some(secret)

    	when(userDao.selectByEmail(email)) thenReturn Future.successful(None) 
    	when(userDao.insert(any[User])) thenReturn Future.successful(Success(user))

    	
      val requestBody = Json.obj( "email" -> email, "password" -> password )
      
      
      
      val request = FakeRequest(POST, "/api/signup", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      val result = controller.signup().apply(request)
      
      val json = contentAsJson(result)
      
      verify(userDao).insert(any[User])
      
      
      (json \ "request").asOpt[String] mustBe Some( "signup" )
    	(json \ "cause").asOpt[String] mustBe None
      (json \ "status").asOpt[String] mustBe Some("OK")
      (json \ "user" \ "token").as[String] mustNot be(empty)
    }
    
    "not register a user twice" in { 
    	when(userDao.insert(any[User])) thenReturn Future.successful(Failure(InsertDuplicateException("USERS", "email", email)))
      
      val requestBody = Json.obj( "email" -> email, "password" -> password )
      
      val request = FakeRequest(POST, "/api/signup", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
      
      val result = controller.signup().apply(request)
      
      status(result) mustBe OK
      
      val json = contentAsJson(result)
      
      
      (json \ "request").asOpt[String] mustBe Some( "signup" )
      (json \ "status").asOpt[String] mustBe Some("KO")
      (json \ "token").asOpt[String] mustBe None
    }  
      
  }
  
  
  "login" should {
    
    "need an email" in { 
      val requestBody = Json.obj( "password" -> password )
      val request = FakeRequest(POST, "/api/login", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      
      val result = controller.signup().apply(request)
      
      val BAD_REQUEST_CODE = 400
      result.onComplete { 
        case Success(r) => r.header.status must be (BAD_REQUEST_CODE)
        case Failure(e) => fail 
        }
    }
    
    "login if the email/password pair is correct" in { 
      when(configuration.getOptional[String]("play.crypto.secret")) thenReturn Some(secret)

    	when(userDao.selectByEmail(email)) thenReturn Future.successful(Some(user)) 
    	
      val requestBody = Json.obj( "email" -> email, "password" -> password )
      
      val request = FakeRequest(POST, "/api/login", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      val result = controller.login().apply(request)
      
      val json = contentAsJson(result)
      
      verify(userDao).selectByEmail(email)
      
      (json \ "request").asOpt[String] mustBe Some( "login" )
      (json \ "status").asOpt[String] mustBe Some("OK")
      (json \ "token").as[String] mustNot be(empty)
      (json \ "user" \ "email").asOpt[String] mustBe Some(email)
      (json \ "user" \ "role").asOpt[String] mustBe Some(role)
      
    }
    
    "check the email's existence" in {
      when(configuration.getOptional[String]("play.crypto.secret")) thenReturn Some(secret)

    	when(userDao.selectByEmail(email)) thenReturn Future.successful(None) 
    	
      val requestBody = Json.obj( "email" -> email, "password" -> password )
      
      val request = FakeRequest(POST, "/api/login", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      val result = controller.login().apply(request)
      
      val json = contentAsJson(result)
      
      verify(userDao).selectByEmail(email)
      
      (json \ "request").asOpt[String] mustBe Some( "login" )
      (json \ "status").asOpt[String] mustBe Some("KO")
      (json \ "token").asOpt[String] mustBe None
      (json \ "cause").asOpt[String] mustBe Some("email not found")
    }
    
    "check the password" in {
      when(configuration.getOptional[String]("play.crypto.secret")) thenReturn Some(secret)

    	when(userDao.selectByEmail(email)) thenReturn Future.successful(Some(user)) 
    	
      val requestBody = Json.obj( "email" -> email, "password" -> "not_password" )
      
      val request = FakeRequest(POST, "/api/login", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      val result = controller.login().apply(request)
      
      val json = contentAsJson(result)
      
      verify(userDao).selectByEmail(email)
      
      (json \ "request").asOpt[String] mustBe Some( "login" )
      (json \ "status").asOpt[String] mustBe Some("KO")
      (json \ "token").asOpt[String] mustBe None
      (json \ "cause").asOpt[String] mustBe Some("wrong password")
    }
    
  }
  
  
}