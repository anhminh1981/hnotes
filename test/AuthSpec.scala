import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.Headers
import play.api.libs.json.Json
import scala.concurrent.Future
import play.api.mvc.Result
import org.scalatestplus.play.guice.GuiceOneAppPerSuite


class AuthSpec extends PlaySpec with GuiceOneAppPerSuite {
  
  def checkKo(result: Future[Result], method: String , expectedCause: String): Unit = {
    status(result) mustBe OK
      
    contentType(result) mustBe Some("application/json")
       
    val json = contentAsJson(result)
       
    (json \ "request").asOpt[String] mustBe Some( method )
    ((json \ "status").asOpt[String], (json \ "cause").asOpt[String]) mustBe (Some("KO"), Some(expectedCause))
    (json \ "token").asOpt[String] mustBe None
  }
  
  def checkOk(result: Future[Result], method: String) {
    status(result) mustBe OK
      
    contentType(result) mustBe Some("application/json")
     
    val json = contentAsJson(result)
     
    (json \ "request").asOpt[String] mustBe Some( method )
  	((json \ "status").asOpt[String], (json \ "cause").asOpt[String]) mustBe (Some("OK"), None)
    (json \ "token").asOpt[String].getOrElse("") mustNot be(empty)
  }
  
  
  
   "signup" must {
     val requestBody = Json.obj( "email" -> "a@a.a", "password" -> "Aa123_4567" )
      
     val request = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
         body =  requestBody )
     
     "signup a user" in {
      
      val result = route(app, request).get
       
      checkOk(result, "signup")
      
     }
     
     "refuse a second identical request" in {
       val result = route(app, request).get
       
      checkKo(result, "signup", "email already registered")
      
     }
     
     "check the email format" in {
       val requestBodyF = Json.obj( "email" -> "a_a.a", "password" -> "Aa123_4567" )
      
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", "wrong email format")
     }
     
     val passwordError = "password must be 6 to 20 characters long, and must contain lower case, upper case, digit and special characters"
     "check the password is more than 5 characters" in {
       val requestBodyF = Json.obj( "email" -> "a@a.a", "password" -> "aB3!!" )
    
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", passwordError)
     }
     
     "check the password is less than 21 characters" in {
       val requestBodyF = Json.obj( "email" -> "a@a.a", "password" -> "aB3!!aB3!!aB3!!aB3!!a" )
    
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", passwordError)
     }
     
     "check the password has at least one lower case character" in {
       val requestBodyF = Json.obj( "email" -> "a@a.a", "password" -> "ABC123!" )
    
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", passwordError)
     }
     
     "check the password has at least one upper case character" in {
       val requestBodyF = Json.obj( "email" -> "a@a.a", "password" -> "abc123!" )
    
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", passwordError)
     }
     
     "check the password has at least one digit" in {
       val requestBodyF = Json.obj( "email" -> "a@a.a", "password" -> "abcabc!" )
    
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", passwordError)
     }
     
     "check the password has at least one special character" in {
       val requestBodyF = Json.obj( "email" -> "a@a.a", "password" -> "abc123" )
    
       val requestF = FakeRequest(POST, "/api/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
           body =  requestBodyF )
       val result = route(app, requestF).get
       checkKo(result, "signup", passwordError)
     }
     
   }
   
   "login" should {
     "login a user" in {
      val requestBody = Json.obj( "email" -> "a@a.a", "password" -> "Aa123_4567" )
      
      val request = FakeRequest(POST, "/api/login", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
          body =  requestBody )
      
       
      val result = route(app, request).get
       
      
      status(result) mustBe OK
      
      contentType(result) mustBe Some("application/json")
       
      val json = contentAsJson(result)
       
      (json \ "request").asOpt[String] mustBe Some( "login" )
    	(json \ "cause").asOpt[String] mustBe None
      (json \ "status").asOpt[String] mustBe Some("OK")
      (json \ "token").asOpt[String].getOrElse("") mustNot be(empty)
     }
     
     "refuse a bad password" in {
       val requestBody = Json.obj( "email" -> "a@a.a", "password" -> "b" )
      
      val request = FakeRequest(POST, "/api/login", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
          body =  requestBody )
      
       
      val result = route(app, request).get
       
      
      status(result) mustBe OK
      
      contentType(result) mustBe Some("application/json")
       
      val json = contentAsJson(result)
       
      (json \ "request").asOpt[String] mustBe Some( "login" )
    	(json \ "cause").asOpt[String] mustBe Some("wrong password")
      (json \ "status").asOpt[String] mustBe Some("KO")
      (json \ "token").asOpt[String] mustBe None 
     }
   }
   
}