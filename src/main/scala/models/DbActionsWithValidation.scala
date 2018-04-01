package models

import com.typesafe.config.ConfigFactory
import models.DbActions._
import models.SortType.SortType
import scala.concurrent.Future

object DbActionsWithValidation {

  val confLimit: Int = ConfigFactory.load().getInt("constrains.maxListing")

  def createUserWithValidation(user: User): Option[Future[Int]] = {
    if (validateUserParams(user)) {
      Some(createUser(user))
    } else {
      None
    }
  }

  def createTopicWithValidation(topic: Topic, userID: Int): Option[Future[Response]] = {
    if (validateContent(topic)) {
      Some(createTopic(topic: Topic, userID: Int))
    } else {
      None
    }
  }

  def createAnswerWithValidation(answer: Answer, topicID: Int, userID: Int): Option[Future[Response]] = {
    if (validateContent(answer)) {
      Some(createAnswer(answer: Answer, topicID: Int, userID: Int))
    } else {
      None
    }
  }

  def getTopicsWithValidation(sort: SortType, limit: Int, offset: Int) = {
    if (validatePaginationTopics(limit, offset)) {
      DbActions.getTopics(sort, limit, offset)
    } else {
      DbActions.getTopics(sort, confLimit, offset)
    }
  }

  def getAnswersWithValidation(id: Int, mid: Int, before: Int, after: Int) = {
    if (validatePaginationAnswers(before, after)) {
      DbActions.getAnswers(id, mid, before, after)
    } else {
      val pagination = getCorrectPaginationAnswers(before, after)
      DbActions.getAnswers(id, mid, pagination._1, pagination._2)
    }
  }

  private def validatePaginationTopics(limit: Int, offset: Int) = {
    limit < confLimit
  }

  private def validatePaginationAnswers(before: Int, after: Int) = {
    before + after + 1 < confLimit
  }

  private def getCorrectPaginationAnswers(before: Int, after: Int) = {
    val newBefore: Double = before/(before+after)
    val newAfter: Double = after/(before+after)
    ((confLimit*newBefore).toInt, (confLimit*newAfter).toInt)
  }

  private def validateUserParams(user: User) = {
    user.nick.length > 2 && user.nick.length < 20 &&
    user.email.contains('@') && user.email.length > 5 &&
    user.email.length < 30
  }

  private def validateContent(contents: Contents) = contents match {
    case t: Topic => t.subject.length > 1 && t.subject.length < 200 &&
     t.content.length > 3 && t.content.length < 1000
    case a: Answer => a.content.length > 1 && a.content.length < 1000
  }

}
