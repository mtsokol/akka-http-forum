import org.scalatest.{FlatSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest

class ExampleSpec extends FlatSpec with Matchers with ScalatestRouteTest {

  "Test" should "pass" in {
    (1 + 2) should be (3)
  }

  "Service" should "respond to single IP query" in {
    Get() ~> routing.Service.route ~> check {
      status shouldBe OK
      //contentType shouldBe `application/json`
    }
  }

}
