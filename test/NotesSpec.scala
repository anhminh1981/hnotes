import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.libs.json.JsObject


class NotesSpec extends PlaySpec with OneAppPerSuite with TestData  {
  insertData
  val token = {
    val requestBody = Json.obj( "email" -> "test@test.test", "password" -> "Test_123" )
      
    val request = new FakeRequest(POST, "/login", headers = FakeHeaders(Seq("Content-Type" -> "application/json")),
        body =  requestBody )
     
    val result = route(app, request).get
    
    (contentAsJson(result) \ "token").as[String]
  }
  
  "notes" should {
    "refuse requests without authorization" in {
      val request = FakeRequest(GET, "/notes")
      
      val result = route(app, request).get
      
      status(result) mustBe UNAUTHORIZED
    }
    
    "return the list of notes owned by the user designated by the token" in {
      val request = new FakeRequest(GET, "/notes", headers = FakeHeaders(Seq("Authorization" -> token)), body = "")
      
      val result = route(app, request).get
      
      status(result) mustBe OK
      
      contentType(result) mustBe Some("application/json")
     
      val json = contentAsJson(result)
      
      (json \ "notes").asOpt[Array[JsObject]] must not be None 
      
      (json \ "notes").as[Array[JsObject]].length mustBe 2
    
    }
    
  }
  
  "/notes/id" should {
    "forbid the consultation of notes not owned by the user" in {
      val request = new FakeRequest(GET, "/notes/" + noteId3, headers = FakeHeaders(Seq("Authorization" -> token)), body = "")
      val result = route(app, request).get
      
      status(result) mustBe FORBIDDEN
    }
    "return the note if it's owned by the user" in {
      val request = new FakeRequest(GET, "/notes/" + noteId2, headers = FakeHeaders(Seq("Authorization" -> token)), body = "")
      val result = route(app, request).get
      
      status(result) mustBe OK
      
      contentType(result) mustBe Some("application/json")
     
      val json = contentAsJson(result)
      
      (json \ "title").as[String] mustBe "title2"
      (json \ "text").as[String] mustBe "lore ipsum"
      
    }
  }
}