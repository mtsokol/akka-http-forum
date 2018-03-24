package routing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import controllers.Controller._
import models._
import spray.json._

import scala.util.Success

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val answerFormat = jsonFormat6(Answer)
  implicit val topicFormat = jsonFormat6(Topic)
}

object Service extends Directives with JsonSupport {

  def main(args: Array[String]) {

    implicit val system = ActorSystem("system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route = {
      path("") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html.index().toString()))
        }
      } ~
      path("posting") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html.posting().toString()))
        }
      } ~
      pathPrefix("topics") {
        pathEnd {
          get {
            parameters('sort ? "latest", 'limit ? 20, 'offset ? 0) { (sort, limit, offset) =>
              onComplete(getTopics(sort, limit, offset)) {
                case Success(value) =>
                  complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html.topics(value).toString()))
                case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "failure"))
              }
            }
          } ~
            post {
              entity(as[Topic]) { topic =>
                println(topic)
                onComplete(createTopic(topic)) {
                  case Success(xd) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"$xd post topic"))
                  case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"error"))
                }
              }
            }
        } ~
          pathPrefix(IntNumber) { (topicID) =>
            pathEnd {
              get {
                parameters('mid ? 1, 'before ? 0, 'after ? 50) { (mid, before, after) =>
                  onComplete(getTopics("test", before, after)) {
                    case Success(value) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, value.toString()))
                    case _ => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "failure"))
                  }
                }
              } ~
                headerValueByName("WWW-Authenticate") { secret =>
                  put {
                    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"modify topic"))
                  } ~
                    delete {
                      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"delete topic"))
                    }
                }
            } ~
              pathPrefix("answers") {
                pathEnd {
                  post {
                    entity(as[Answer]) { answer =>
                      println(answer.content)
                      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"add answer"))
                    }
                  }
                } ~
                  path(IntNumber) { (answerID) =>
                    headerValueByName("WWW-Authenticate") { secret =>
                      put {
                        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"modify answer"))
                      } ~
                        delete {
                          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"delete answer"))
                        }
                    }
                  }
              }
          }
      }
    }

    val config = ConfigFactory.load()

    //val bindingFuture = Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))
    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)

    println(s"Running on port ${config.getInt("http.port")}...")

  }
}
