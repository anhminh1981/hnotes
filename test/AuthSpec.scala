import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.Headers
import play.api.libs.json.Json


class AuthSpec extends PlaySpec with OneAppPerSuite {
   "signup" must {
     val requestBody = Json.obj( "email" -> "a@a.a", "password" -> "a" )
      
     val request = new FakeRequest(POST, "/signup", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
         body =  requestBody )
     
     "signup a user" in {
      
      val result = route(app, request).get
       
      
      status(result) mustBe OK
      
      contentType(result) mustBe Some("application/json")
       
      val json = contentAsJson(result)
       
      (json \ "request").asOpt[String] mustBe Some( "signup" )
    	(json \ "cause").asOpt[String] mustBe None
      (json \ "status").asOpt[String] mustBe Some("OK")
      (json \ "token").asOpt[String].getOrElse("") mustNot be(empty)
     }
     
     "refuse a second identical request" in {
       val result = route(app, request).get
       
      
      status(result) mustBe OK
      
      contentType(result) mustBe Some("application/json")
       
      val json = contentAsJson(result)
       
      (json \ "request").asOpt[String] mustBe Some( "signup" )
    	(json \ "cause").asOpt[String] mustBe Some("email already registered")
      (json \ "status").asOpt[String] mustBe Some("KO")
      (json \ "token").asOpt[String] mustBe None
     }
     
     
     
   }
   
   "login" should {
     "login a user" in {
      val requestBody = Json.obj( "email" -> "a@a.a", "password" -> "a" )
      
      val request = new FakeRequest(POST, "/login", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
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
      
      val request = new FakeRequest(POST, "/login", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
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