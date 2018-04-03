package routing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import controllers.Controller._
import models._
import spray.json._
import scala.util.{Failure, Success}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(UserInput)
  implicit val answerFormat = jsonFormat2(AnswerInput)
  implicit val topicFormat = jsonFormat3(TopicInput)
}

object Routing extends Directives with JsonSupport {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val content = ContentTypes.`text/html(UTF-8)`

  val route = {
    path("") {
      get {
        complete(OK, HttpEntity(content, html.index().toString))
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
            complete(OK, HttpEntity(content, html.posting(topic).toString))
          }
        }
      } ~
      pathPrefix("topics") {
        pathEnd {
          get {
            parameters('sort ? "latest", 'limit ? 20, 'offset ? 0) { (sort, limit, offset) =>
              onComplete(getTopics(SortType.parse(sort), limit, offset)) {
                case Success(value) =>
                  complete(OK, HttpEntity(content, html.topics(value).toString))
                case _ => complete(InternalServerError, HttpEntity(content, "internal error"))
              }
            }
          } ~
            post {
              entity(as[TopicInput]) { topic =>
                onComplete(createTopic(topic)) {
                  case Success(response) => response match {
                    case Success(msg) => complete(Created, msg)
                    case Failure(ex) => complete(BadRequest, ex.getMessage)
                  }
                  case _ => complete(InternalServerError, "internal error")
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
                      case Some(tuple) => complete(OK, HttpEntity(content,
                        html.topic(topicID, tuple._1, tuple._2).toString))
                      case None => complete(NotFound, HttpEntity(content, html.error404().toString))
                    }
                    case _ => complete(InternalServerError, HttpEntity(content, "internal error"))
                  }
                }
              } ~
                headerValueByName("WWW-Authenticate") { secret =>
                  put {
                    entity(as[String]) { content =>
                      onComplete(modifyTopic(topicID, secret, content)) {
                        case Success(value) => value match {
                          case Some(status) =>
                            complete(Created, s"topic modified $status")
                          case None =>
                            complete(Unauthorized, "invalid secret")
                        }
                        case _ => complete(InternalServerError, "internal error")

                      }
                    }
                  } ~
                    delete {
                      onComplete(deleteTopic(topicID, secret)) {
                        case Success(value) => value match {
                          case Some(status) =>
                            complete(NoContent, s"topic deleted $status")
                          case None =>
                            complete(Unauthorized, "invalid secret")
                        }
                        case _ => complete(InternalServerError, "internal error")
                      }
                    }
                }
            } ~
              pathPrefix("answers") {
                pathEnd {
                  post {
                    entity(as[AnswerInput]) { answer =>
                      onComplete(createAnswer(answer, topicID)) {
                        case Success(response) => response match {
                          case Success(msg) => complete(Created, msg)
                          case Failure(ex) => complete(BadRequest, ex.getMessage)
                        }
                        case _ => complete(InternalServerError, "internal error")
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
                                complete(Created, s"modified answer $status")
                              case None =>
                                complete(Unauthorized, "invalid secret")
                            }
                            case _ =>
                              complete(InternalServerError, "internal error")
                          }
                        }

                      } ~
                        delete {
                          onComplete(deleteAnswer(answerID, secret)) {
                            case Success(value) => value match {
                              case Some(status) =>
                                complete(NoContent, s"answer deleted $status")
                              case None =>
                                complete(Unauthorized, "invalid secret")
                            }
                            case _ => complete(InternalServerError, "internal error")
                          }
                        }
                    }
                  }
              }
          }
      } ~
      pathPrefix("") {
        get {
          complete(NotFound, HttpEntity(content, html.error404().toString))
        }
      }
  }

  def main(args: Array[String]) {

    val config = ConfigFactory.load()
    Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))

    println(s"Running on port ${config.getInt("http.port")}...")
  }
}
