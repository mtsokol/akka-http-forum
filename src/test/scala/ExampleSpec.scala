import akka.http.scaladsl.model._
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.ScalatestRouteTest
import routing.Routing.route
import RequestHelpers._
import models.ContentType._
import scala.collection._

class ExampleSpec extends WordSpec with Matchers with ScalatestRouteTest {

  routing.Routing.main(Array())

  "Service" should {
    "respond with index page" in {
      Get() ~> route ~> check {
        status shouldBe OK
      }
    }

    "respond with topic and answers" in {
      getTopics(getTopicsRequest).map {
        id =>
          Get(s"/topics/$id") ~> route ~> check {
            status shouldBe OK
          }
      }
    }

    "respond with 404 error" in {
      Get("/topics/12345") ~> route ~> check {
        status shouldBe NotFound
      }
    }

    "post topic and return secret" in {
      postTopicRequest(json) ~> route ~> check {
        status shouldBe Created
        responseAs[String].length shouldBe 7
      }
    }

    "respond with topics list" in {
      Get("/topics") ~> route ~> check {
        status shouldBe OK
      }
    }

    "modify topic" in {
      insertContent(Topics).map {
        result =>
          val putRequest = HttpRequest(
            HttpMethods.PUT,
            uri = s"/topics/${result._1}",
            headers = immutable.Seq(RawHeader("WWW-Authenticate", s"${result._2}").asInstanceOf[HttpHeader]))
          putRequest ~> route ~> check {
            status shouldBe Created
          }
      }
    }

    "delete topic" in {
      insertContent(Topics).map {
        result =>
          val deleteRequest = HttpRequest(
            HttpMethods.DELETE,
            uri = s"/topics/${result._1}",
            headers = immutable.Seq(RawHeader("WWW-Authenticate", s"${result._2}").asInstanceOf[HttpHeader]))

          deleteRequest ~> route ~> check {
            status shouldBe NoContent
          }
      }
    }

    "return invalid secret error" in {
      insertContent(Topics).map {
        result =>
          val deleteRequest = HttpRequest(
            HttpMethods.DELETE,
            uri = s"/topics/${result._1}",
            headers = immutable.Seq(RawHeader("WWW-Authenticate", "ASDFGHJ").asInstanceOf[HttpHeader]))

          deleteRequest ~> route ~> check {
            status shouldBe Unauthorized
          }
      }
    }

    "return invalid user params error" in {
      postTopicRequest(invalidJsonUser) ~> route ~> check {
        status shouldBe BadRequest
        responseAs[String] shouldBe "Invalid user params"
      }
    }

    "return invalid content params error" in {
      postTopicRequest(invalidJsonContent) ~> route ~> check {
        status shouldBe BadRequest
        responseAs[String] shouldBe "Invalid topic params"
      }
    }

    "post answer and return secret" in {
      insertContent(Topics).map {
        result =>
          postAnswerRequest(result._1) ~> route ~> check {
            status shouldBe Created
            responseAs[String].length shouldBe 7
          }
      }
    }

    "modify answer" in {
      insertContent(Answers).map {
        result =>
          getTopics(getTopicsRequest).map {
            topicID =>
              val putRequest = HttpRequest(
                HttpMethods.PUT,
                uri = s"/topics/${result._1}/answers/$topicID",
                headers = immutable.Seq(RawHeader("WWW-Authenticate", s"${result._2}").asInstanceOf[HttpHeader]))
              putRequest ~> route ~> check {
                status shouldBe Created
              }
          }
      }
    }

    "delete answer" in {
      insertContent(Answers).map {
        result =>
          getTopics(getTopicsRequest).map {
            topicID =>
              val putRequest = HttpRequest(
                HttpMethods.DELETE,
                uri = s"/topics/${result._1}/answers/$topicID",
                headers = immutable.Seq(RawHeader("WWW-Authenticate", s"${result._2}").asInstanceOf[HttpHeader]))
              putRequest ~> route ~> check {
                status shouldBe Created
              }
          }
      }
    }

  }

}
