import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import routing.Routing.route
import scala.util.{ Failure, Success }
import scala.concurrent.Future

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

  var id = 0
  var body = ""
  var secret = ""


  def addContent() = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://akka.io", entity = jsonRequest))

    responseFuture
      .onComplete {
        case Success(res) => println(res.entity)
        case Failure(_)   => sys.error("something wrong")
      }
  }

  def spliting(body: String) = {
    body.split("topics/").tail.head.split("\"").head.toInt
  }

  "Service" should {
    "respond with index page" in {
      Get() ~> route ~> check {
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
        secret = responseAs[String]
        responseAs[String].length shouldBe 7
      }
    }

    "respond with topics list" in {
      Get("/topics") ~> route ~> check {
        body = responseAs[String]
        id = spliting(body)
        status shouldBe OK
      }
    }

    "modify topic" in {
      val putRequest = HttpRequest(
        HttpMethods.PUT,
        uri = s"/topics/$id",
        headers = scala.collection.immutable.Seq(RawHeader("WWW-Authenticate",s"$secret").asInstanceOf[HttpHeader]))

      putRequest ~> route ~> check {
        status shouldBe Created
      }
    }

    "delete topic" in {
      val deleteRequest = HttpRequest(
        HttpMethods.DELETE,
        uri = s"/topics/$id",
        headers = scala.collection.immutable.Seq(RawHeader("WWW-Authenticate",s"$secret").asInstanceOf[HttpHeader]))

      deleteRequest ~> route ~> check {
        status shouldBe NoContent
      }
    }

    "return invalid secret error" in {

    }

    "return invalid user params error" in {

    }

    "return invalid content params error" in {

    }

    "post answer and return secret" in {

    }

    "modify answer" in {

    }

    "delete answer" in {

    }

  }

}
