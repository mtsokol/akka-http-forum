import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import routing.Routing._
import scala.concurrent.Future
import models.ContentType.ContentType
import models.ContentType._

object RequestHelpers {

  val config = ConfigFactory.load()
  val url = s"http://${config.getString("http.interface")}:${config.getInt("http.port")}"

  val json = ByteString(
    s"""
       |{"user":{"nick":"testnick","email":"test@mail.com"},
       |"subject":"testsubject","content":"testcontent"}
        """.stripMargin)

  val invalidJsonUser = ByteString(
    s"""
       |{"user":{"nick":"","email":"testmail.com"},
       |"subject":"testsubject","content":"testcontent"}
        """.stripMargin)

  val invalidJsonContent = ByteString(
    s"""
       |{"user":{"nick":"testnick","email":"test@mail.com"},
       |"subject":"","content":""}
        """.stripMargin)

  def postTopicRequest(json: ByteString) = HttpRequest(
    HttpMethods.POST,
    uri = s"$url/topics",
    entity = HttpEntity(MediaTypes.`application/json`, json))

  def postAnswerRequest(topicID: Int) = HttpRequest(
    HttpMethods.POST,
    uri = s"$url/topics/$topicID/answers",
    entity = HttpEntity(MediaTypes.`application/json`, json))

  val getTopicsRequest = HttpRequest(uri = s"$url/topics")

  def getTopicRequest(topicID: Int) = HttpRequest(uri = s"$url/topics/$topicID")

  def insertContent(typee: ContentType): Future[(Int, String)] = {
    typee match {
      case Topics => addContent(postTopicRequest(json), getTopicsRequest)
      case Answers => addContent(postTopicRequest(json), getTopicsRequest).flatMap(result =>
        addContent(postAnswerRequest(result._1), getTopicRequest(result._1))
      )
    }
  }

  private def addContent(f: HttpRequest, s: HttpRequest) = {
    val postTopicFuture = for {
      response <- Http().singleRequest(f)
      entity <- Unmarshal(response.entity).to[String]
    } yield entity

    val getTopicsFuture = getTopics(s)

    postTopicFuture.flatMap(secret => getTopicsFuture.map(id => (id, secret)))
  }

  def getTopics(s: HttpRequest): Future[Int] = for {
    response <- Http().singleRequest(s)
    entity <- Unmarshal(response.entity).to[String]
  } yield split(entity)

  private def split(body: String) = {
    body.split("topics/").tail.head.split("\"").head.toInt
  }

}
