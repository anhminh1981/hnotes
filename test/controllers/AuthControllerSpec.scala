package controllers

import scala.concurrent.ExecutionContext.Implicits._

import org.joda.time.DateTimeUtils
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import org.scalatestplus.play.OneAppPerTest

import dao.UserDao
import models.User
import play.api.Configuration
import scala.concurrent._
import play.api.test._
import play.api.mvc.Headers
import play.api.libs.json.JsString
import play.api.libs.json.Json
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Failure
import org.scalatest.BeforeAndAfterEach
import com.github.t3hnar.bcrypt._


class AuthControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {
  // default values
  val email = "test@test.test"
  val password = "password"
  val role = "user"
  val user = User(0, email, password, role)
  //Mocking the secret
  val secret = "changeme"
  val config = mock[Configuration]
  
  var userDao = mock[UserDao]
  var controller = new AuthController(userDao, config)
  
  override def beforeEach() = { 
    userDao = mock[UserDao]
    controller = new AuthController(userDao, config)
  }
  
  "createToken" must { 
    "create a token if there's a secret" in { 
      
      when(config.getString("play.crypto.secret")) thenReturn Some(secret)
      
      // Mocking the time
      val provider = mock[DateTimeUtils.MillisProvider]
      when(provider.getMillis) thenReturn 1472289657315L
      DateTimeUtils.setCurrentMillisProvider(provider)
      
      // Expected result
      val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
      val claim = "eyJpYXQiOjE0NzIyODk2NTczMTUsImlzcyI6Imhub3RlcyIsImVtYWlsIjoidGVzdEB0ZXN0LnRlc3QiLCJyb2xlIjoidXNlciJ9"
      val signature = "vOVqafgoAtF_vGHBE1Hlg00Zy91tvwhBwNJY0eOnkwk"
      
      // Running the method
      val controller = new AuthController(null, config)
      val token = controller.createToken(user) 
      
      // Result
      token must equal(s"$header.$claim.$signature")
      verify(config, atLeastOnce()).getString("play.crypto.secret")
    }
    "throw an exception if the secret's not configured" in {
      val config = mock[Configuration]
      when(config.getString("play.crypto.secret")) thenReturn None
      val controller = new AuthController(null, config)
      an [IllegalStateException] must be thrownBy controller.createToken(User(1, "test@test.test", "password", "user")) 
    }
  }
  
  "signup" should {
    
    "need an email" in { 
      val requestBody = Json.obj( "password" -> password )
      val request = new FakeRequest(POST, "/signup", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      
      val result = controller.signup().apply(request)
      
      val BAD_REQUEST_CODE = 400
      result.onComplete { 
        case Success(r ) => r.header.status must be (BAD_REQUEST_CODE)
        case Failure(e) => fail 
        }
    }
    
    
    "register a new user" in { 
    	when(config.getString("play.crypto.secret")) thenReturn Some(secret)

    	when(userDao.selectByEmail(email)) thenReturn Future.successful(Seq()) 
    	when(userDao.insert(any[User])) thenReturn Future.successful(user)

    	
      val requestBody = Json.obj( "email" -> email, "password" -> password )
      
      
      
      val request = new FakeRequest(POST, "/signup", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
  
      
      val result = controller.signup().apply(request)
      
      val json = contentAsJson(result)
      
      verify(userDao).selectByEmail(email)
      verify(userDao).insert(any[User])
      
      
      
      (json \ "request").asOpt[String] mustBe Some( "signup" )
      (json \ "status").asOpt[String] mustBe Some("OK")
      (json \ "token").as[String] mustNot be(empty)
    }
    
    "not register a user twice" in { 
    	when(userDao.selectByEmail(email)) thenReturn Future.successful(Seq(user))
      
      val requestBody = Json.obj( "email" -> email, "password" -> password )
      
      val request = new FakeRequest(POST, "/signup", headers = Headers("Content-Type" -> "application/json"),
          body =  requestBody )
      
      val result = controller.signup().apply(request)
      
      val json = contentAsJson(result)
      
      verify(userDao).selectByEmail(email)
      
      verify(userDao, never()).insert(any[User])
      
      
      
      (json \ "request").asOpt[String] mustBe Some( "signup" )
      (json \ "status").asOpt[String] mustBe Some("KO")
      (json \ "token").asOpt[String] mustBe None
    }  
      
  }
  
  
}