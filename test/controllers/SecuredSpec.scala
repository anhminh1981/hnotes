package controllers

import scala.concurrent.ExecutionContext.Implicits._

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._

import play.api.test._
import play.api.mvc.Headers
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.Configuration
import scala.util.Failure
import scala.util.Success
import models.User
import play.api.mvc.Results

class SecuredSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {
  val secret = "changeme"
  val config = mock[Configuration]
  when(config.getString("play.crypto.secret")) thenReturn Some(secret)
  
  
  "secure" should { 
    "reject requests without tokens" in { 
      val request = FakeRequest(GET, "/test" )
      val secure = new Secured(config)
      val result = secure.UserAction.transform(request)
      
      result.onComplete { 
        case Failure(_) => fail
        case Success(r) => r.user mustBe None
      }
    }
    
    "read the token" in { 
      val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
      val claim = "eyJpYXQiOjE0NzIyODk2NTczMTUsImlzcyI6Imhub3RlcyIsImVtYWlsIjoidGVzdEB0ZXN0LnRlc3QiLCJyb2xlIjoidXNlciJ9"
      val signature = "vOVqafgoAtF_vGHBE1Hlg00Zy91tvwhBwNJY0eOnkwk"
      
      val request = new FakeRequest(GET, "/test" , headers = Headers("authorization" -> s"$header.$claim.$signature"), body = "")
      val secure = new Secured(config)
      val result = secure.UserAction.transform(request)
      
      result.onComplete { 
        case Failure(_) => fail
        case Success(r) => r.user mustBe Some(User(0, "test@test.test", null, "user"))
      }
    }
    
    "respond with a 401 to requests without user" in { 
      
      val request = new UserRequest(None, FakeRequest(GET, "/test" ))
      val secure = new Secured(config)
      
      val result = secure.PermissionCheckAction.filter(request)
      
      result.onComplete { 
        case Failure(_) => fail
        case Success(r) => r mustBe Some(Results.Unauthorized)
      }
    }
    
    "let requests with user through" in { 
      
      val request = new UserRequest(Some(User(0, "test@test.test", null, "user")), FakeRequest(GET, "/test" ))
      val secure = new Secured(config)
      
      val result = secure.PermissionCheckAction.filter(request)
      
      result.onComplete { 
        case Failure(_) => fail
        case Success(r) => r mustBe None
      }
    }
  }
}