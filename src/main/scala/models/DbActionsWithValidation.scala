package models

import com.typesafe.config.{Config, ConfigFactory}
import models.DbActions._
import models.SortType.SortType
import scala.concurrent.Future
import scala.util.Try

object DbActionsWithValidation {

  val config: Config = ConfigFactory.load()
  val maxListing: Int = config.getInt("constrains.maxListing")
  val emailMin: Int = config.getInt("constrains.emailMin")
  val emailMax: Int = config.getInt("constrains.emailMax")
  val userMin: Int = config.getInt("constrains.userMin")
  val userMax: Int = config.getInt("constrains.userMax")
  val subjectMin: Int = config.getInt("constrains.subjectMin")
  val subjectMax: Int = config.getInt("constrains.subjectMax")
  val contentMin: Int = config.getInt("constrains.contentMin")
  val contentMax: Int = config.getInt("constrains.contentMax")

  def createUserWithValidation(user: UserInput): Option[Future[Int]] = {
    if (validateUserParams(user)) {
      Some(createUser(user))
    } else {
      None
    }
  }

  def createTopicWithValidation(topic: TopicInput, userID: Int): Option[Future[Try[String]]] = {
    if (validateContent(topic)) {
      Some(createTopic(topic: TopicInput, userID: Int))
    } else {
      None
    }
  }

  def createAnswerWithValidation(answer: AnswerInput, topicID: Int, userID: Int): Option[Future[Try[String]]] = {
    if (validateContent(answer)) {
      Some(createAnswer(answer: AnswerInput, topicID: Int, userID: Int))
    } else {
      None
    }
  }

  def getTopicsWithValidation(sort: SortType, limit: Int, offset: Int): Future[Seq[Topic]] = {
    if (validatePaginationTopics(limit, offset)) {
      DbActions.getTopics(sort, limit, offset)
    } else {
      DbActions.getTopics(sort, maxListing, offset)
    }
  }

  def getAnswersWithValidation(id: Int, mid: Int, before: Int, after: Int): Future[Seq[Answer]] = {
    if (validatePaginationAnswers(before, after)) {
      DbActions.getAnswers(id, mid, before, after)
    } else {
      val pagination = getCorrectPaginationAnswers(before, after)
      DbActions.getAnswers(id, mid, pagination._1, pagination._2)
    }
  }

  private def validatePaginationTopics(limit: Int, offset: Int) = {
    limit < maxListing
  }

  private def validatePaginationAnswers(before: Int, after: Int) = {
    before + after + 1 < maxListing
  }

  private def getCorrectPaginationAnswers(before: Int, after: Int) = {
    val newBefore: Double = before/(before+after)
    val newAfter: Double = after/(before+after)
    ((maxListing*newBefore).toInt, (maxListing*newAfter).toInt)
  }

  private def validateUserParams(user: UserInput) = {
    user.nick.length > userMin && user.nick.length < userMax &&
    user.email.contains('@') && user.email.length > emailMin &&
    user.email.length < emailMax
  }

  private def validateContent(contents: InputContents) = contents match {
    case t: TopicInput => t.subject.length > subjectMin && t.subject.length < subjectMax &&
     t.content.length > contentMin && t.content.length < contentMax
    case a: AnswerInput => a.content.length > contentMin && a.content.length < contentMax
  }

}
