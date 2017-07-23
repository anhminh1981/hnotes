package controllers


import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
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
import org.joda.time.DateTimeUtils
import play.api.Environment
import org.scalatest.mockito.MockitoSugar
import akka.stream.Materializer
import scala.concurrent.ExecutionContext


class SecuredSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {
  val secret = "changeme"
  implicit val config = mock[Configuration]
  implicit val env = mock[Environment]
  implicit val ec = ExecutionContext.global
  when(config.getOptional[String]("play.crypto.secret")) thenReturn Some(secret)
  
  class SecuredImpl(implicit val configuration: Configuration, implicit val env: Environment, implicit val ec: ExecutionContext) extends  Secured {
    implicit val mat = mock[Materializer] 
    
    def test = authenticated {
      Ok
    }
  }
  
  "secure" should { 
    "respond with a 401 to requests without user" in { 
      val request = FakeRequest(GET, "/api/test" )
      val secure = new SecuredImpl
      val result = secure.test(request)
      
      result.onComplete { 
        case Success(r) => r.header.status mustBe Results.Unauthorized.header.status
        case _ => fail
      }
    }
    
    "let requests with user through" in { 
      // Mocking the time
      val provider = mock[DateTimeUtils.MillisProvider]
      when(provider.getMillis) thenReturn 1472289657315L
      DateTimeUtils.setCurrentMillisProvider(provider)
      
      
      val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
      val claim = "eyJpYXQiOjE0NzM4NzQyNDEwMjYsImlzcyI6Imhub3RlcyIsInVzZXJJZCI6Miwicm9sZSI6InVzZXIifQ"
      val signature = "kHOkwcTWLOOikw4PQ0XkD7cUHbb7otiIvV_td1wTnVs"
      
      val request = FakeRequest(GET, "/api/test" , headers = Headers("authorization" -> s"$header.$claim.$signature"), body = "")
      val secure = new SecuredImpl
      val result = secure.test(request)
      
      result map {
        _ mustBe Results.Ok
      }
      
    }
    
  }
  
  "getUserFromRequest" should {
    "read the token and return a user" in {
      // Mocking the time
      val provider = mock[DateTimeUtils.MillisProvider]
      when(provider.getMillis) thenReturn 1472289657315L
      DateTimeUtils.setCurrentMillisProvider(provider)
      
      
      val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
      val claim = "eyJpYXQiOjE0NzM4NzQyNDEwMjYsImlzcyI6Imhub3RlcyIsInVzZXJJZCI6Miwicm9sZSI6InVzZXIifQ"
      val signature = "kHOkwcTWLOOikw4PQ0XkD7cUHbb7otiIvV_td1wTnVs"
      val request = FakeRequest(GET, "/api/test" , headers = Headers("authorization" -> s"Bearer $header.$claim.$signature"), body = "")
      
      val secure = new SecuredImpl
      
      secure.getUserFromRequest(request) mustBe Some(User(2, null, null, "user"))
    }
    "not return a user if there's no token" in {
      val secure = new SecuredImpl
      val request = FakeRequest(GET, "/api/test" )
      
      secure.getUserFromRequest(request) mustBe None
    }
    
    "not return a user if the token's expired" in {
      // Mocking the time
      val provider = mock[DateTimeUtils.MillisProvider]
      when(provider.getMillis) thenReturn 1473874241026L + AuthConstants.duration + 1
      DateTimeUtils.setCurrentMillisProvider(provider)
      
      
      val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
      val claim = "eyJpYXQiOjE0NzM4NzQyNDEwMjYsImlzcyI6Imhub3RlcyIsInVzZXJJZCI6Miwicm9sZSI6InVzZXIifQ"
      val signature = "kHOkwcTWLOOikw4PQ0XkD7cUHbb7otiIvV_td1wTnVs"
      val request = FakeRequest(GET, "/api/test" , headers = Headers("authorization" -> s"$header.$claim.$signature"), body = "")
      
      val secure = new SecuredImpl
      
      secure.getUserFromRequest(request) mustBe None
    }
    "not return a user if the token's signature is invalid" in {
    	// Mocking the time
    	val provider = mock[DateTimeUtils.MillisProvider]
    			when(provider.getMillis) thenReturn 1473874241026L +1 
    			DateTimeUtils.setCurrentMillisProvider(provider)
    			
    			
    			val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
    			val claim = "eyJpYXQiOjE0NzM4NzQyNDEwMjYsImlzcyI6Imhub3RlcyIsInVzZXJJZCI6Miwicm9sZSI6InVzZXIifQ"
    			val signature = "wrongcTWLOOikw4PQ0XkD7cUHbb7otiIvV_td1wTnVs"
    			val request = FakeRequest(GET, "/api/test" , headers = Headers("authorization" -> s"$header.$claim.$signature"), body = "")
    	
    	val secure = new SecuredImpl
    	
    	secure.getUserFromRequest(request) mustBe None
    }
  
  
  }
}