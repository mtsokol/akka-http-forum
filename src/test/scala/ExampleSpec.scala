import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import routing.Service.route

class ExampleSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val jsonRequest = ByteString(
    s"""
       |{"user":{"nick":"testnick","email":"test@mail.com"},
       |"subject":"testsubject","content":"testcontent"}
        """.stripMargin)

  val postRequest = HttpRequest(
    HttpMethods.POST,
    uri = "/topics",
    entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

  "Service" should {

    "respond with index page" in {
      Get() ~> route ~> check {
        status shouldBe OK
      }
    }

    "respond with topics list" in {
      Get("/topics") ~> route ~> check {
        status shouldBe OK
      }
    }

    "respond with topic and answers" in {
      Get("/topics/4") ~> route ~> check {
        status shouldBe OK
      }
    }

    "respond with 404 error" in {
      Get("/topics/1234") ~> route ~> check {
        status shouldBe NotFound
      }
    }

    "post topic and return secret" in {
      postRequest ~> route ~> check {
        status shouldBe Created
      }
    }

  }

}
