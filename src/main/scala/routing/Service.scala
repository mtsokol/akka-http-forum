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
  implicit val userFormat = jsonFormat2(User)
  implicit val answerFormat = jsonFormat2(Answer)
  implicit val topicFormat = jsonFormat3(Topic)
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
      pathPrefix("resources") {
        getFromDirectory("src/main/resources")
      } ~
      path("posting") {
        get {
          parameters('topic ? "") { topic =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html.posting(topic).toString()))
          }
        }
      } ~
      pathPrefix("topics") {
        pathEnd {
          get {
            parameters('sort ? "latest", 'limit ? 20, 'offset ? 0) { (sort, limit, offset) =>
              onComplete(getTopics(sort, limit, offset)) {
                case Success(value) =>
                  complete(200, HttpEntity(ContentTypes.`text/html(UTF-8)`, html.topics(value).toString()))
                case _ => complete(500, HttpEntity(ContentTypes.`text/html(UTF-8)`, "internal error"))
              }
            }
          } ~
            post {
              entity(as[Topic]) { topic =>
                onComplete(createTopic(topic)) {
                  case Success(response) => response match {
                    case models.Success(msg) => complete(201, HttpEntity(ContentTypes.`text/html(UTF-8)`, msg))
                    case Failure(msg) => complete(401, HttpEntity(ContentTypes.`text/html(UTF-8)`, msg))
                  }
                  case _ => complete(500, HttpEntity(ContentTypes.`text/html(UTF-8)`, "internal error"))
                }
              }
            }
        } ~
          pathPrefix(IntNumber) { (topicID) =>
            pathEnd {
              get {
                parameters('mid ? 0, 'before ? 0, 'after ? 50) { (mid, before, after) =>
                  onComplete(getTopic(topicID, mid, before, after)) {
                    case Success(value) => value match {
                      case Some(tuple) => complete(200, HttpEntity(ContentTypes.`text/html(UTF-8)`, html.topic(tuple._1, tuple._2).toString()))
                      case None => complete(404, HttpEntity(ContentTypes.`text/html(UTF-8)`, "No such topic"))
                    }
                    case _ => complete(500, HttpEntity(ContentTypes.`text/html(UTF-8)`, "internal error"))
                  }
                }
              } ~
                headerValueByName("WWW-Authenticate") { secret =>
                  put {
                    entity(as[String]) { content =>
                      onComplete(modifyTopic(topicID, secret, content)) {
                        case Success(value) => value match {
                          case Some(x) => complete(201, HttpEntity(ContentTypes.`text/html(UTF-8)`, s"topic modified $x"))
                          case None => complete(401, HttpEntity(ContentTypes.`text/html(UTF-8)`, s"invalid secret"))
                        }
                        case _ => complete(500, HttpEntity(ContentTypes.`text/html(UTF-8)`, "internal error"))

                      }
                    }
                  } ~
                    delete {
                      onComplete(deleteTopic(topicID, secret)) {
                        case Success(value) => value match {
                          case None =>
                            complete(401, HttpEntity(ContentTypes.`text/html(UTF-8)`, "invalid secret"))
                          case Some(stat) =>
                            complete(204, HttpEntity(ContentTypes.`text/html(UTF-8)`, s"$stat deleted"))
                        }
                        case _ => complete(500, HttpEntity(ContentTypes.`text/html(UTF-8)`, "internal error"))
                      }
                    }
                }
            } ~
              pathPrefix("answers") {
                pathEnd {
                  post {
                    entity(as[Answer]) { answer =>
                      onComplete(createAnswer(answer, topicID)) {
                        case Success(response) => response match {
                          case models.Success(msg) => complete(201, HttpEntity(ContentTypes.`text/html(UTF-8)`, msg))
                          case Failure(msg) => complete(401, HttpEntity(ContentTypes.`text/html(UTF-8)`, msg))
                        }
                        case _ => complete(500, HttpEntity(ContentTypes.`text/html(UTF-8)`, s"internal error"))
                      }

                    }
                  }
                } ~
                  path(IntNumber) { (answerID) =>
                    headerValueByName("WWW-Authenticate") { secret =>
                      put {
                        complete(201, HttpEntity(ContentTypes.`text/html(UTF-8)`, s"modified answer"))
                      } ~
                        delete {
                          complete(204, HttpEntity(ContentTypes.`text/html(UTF-8)`, s"delete answer"))
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
