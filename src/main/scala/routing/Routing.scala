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
import scala.util.{Failure, Success}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(User)
  implicit val answerFormat = jsonFormat2(Answer)
  implicit val topicFormat = jsonFormat3(Topic)
}

object Routing extends Directives with JsonSupport {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val content = ContentTypes.`text/html(UTF-8)`

  val route = {
    path("") {
      get {
        complete(200, HttpEntity(content, html.index().toString))
      }
    } ~
      pathPrefix("css") {
        getFromDirectory("src/main/twirl/css")
      } ~
      pathPrefix("javascript") {
        getFromDirectory("src/main/twirl/javascript")
      } ~
      pathPrefix("images") {
        getFromDirectory("src/main/twirl/images")
      } ~
      path("posting") {
        get {
          parameters('topic ? "") { topic =>
            complete(200, HttpEntity(content, html.posting(topic).toString()))
          }
        }
      } ~
      pathPrefix("topics") {
        pathEnd {
          get {
            parameters('sort ? "latest", 'limit ? 20, 'offset ? 0) { (sort, limit, offset) =>
              onComplete(getTopics(SortType.parse(sort), limit, offset)) {
                case Success(value) =>
                  complete(200, HttpEntity(content, html.topics(value).toString()))
                case _ => complete(500, HttpEntity(content, "internal error"))
              }
            }
          } ~
            post {
              entity(as[Topic]) { topic =>
                onComplete(createTopic(topic)) {
                  case Success(response) => response match {
                    case Success(msg) => complete(201, msg)
                    case Failure(ex) => complete(401, ex.getMessage)
                  }
                  case _ => complete(500, "internal error")
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
                      case Some(tuple) => complete(200, HttpEntity(content,
                        html.topic(topicID, tuple._1, tuple._2).toString))
                      case None => complete(404, HttpEntity(content, html.error404().toString))
                    }
                    case _ => complete(500, HttpEntity(content, "internal error"))
                  }
                }
              } ~
                headerValueByName("WWW-Authenticate") { secret =>
                  put {
                    entity(as[String]) { content =>
                      onComplete(modifyTopic(topicID, secret, content)) {
                        case Success(value) => value match {
                          case Some(status) =>
                            complete(201, s"topic modified $status")
                          case None =>
                            complete(401, "invalid secret")
                        }
                        case _ => complete(500, "internal error")

                      }
                    }
                  } ~
                    delete {
                      onComplete(deleteTopic(topicID, secret)) {
                        case Success(value) => value match {
                          case Some(status) =>
                            complete(204, s"topic deleted $status")
                          case None =>
                            complete(401, "invalid secret")
                        }
                        case _ => complete(500, "internal error")
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
                          case Success(msg) => complete(201, msg)
                          case Failure(ex) => complete(401, ex.getMessage)
                        }
                        case _ => complete(500, "internal error")
                      }

                    }
                  }
                } ~
                  path(IntNumber) { (answerID) =>
                    headerValueByName("WWW-Authenticate") { secret =>
                      put {
                        entity(as[String]) { content =>
                          onComplete(modifyAnswer(answerID, secret, content)) {
                            case Success(value) => value match {
                              case Some(status) =>
                                complete(201, s"modified answer $status")
                              case None =>
                                complete(401, "invalid secret")
                            }
                            case _ =>
                              complete(500, "internal error")
                          }
                        }

                      } ~
                        delete {
                          onComplete(deleteAnswer(answerID, secret)) {
                            case Success(value) => value match {
                              case None =>
                                complete(401, "invalid secret")
                              case Some(status) =>
                                complete(204, s"answer deleted $status")
                            }
                            case _ => complete(500, "internal error")
                          }
                        }
                    }
                  }
              }
          }
      }
  }

  def main(args: Array[String]) {

    val config = ConfigFactory.load()
    Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))

    println(s"Running on port ${config.getInt("http.port")}...")
  }
}
