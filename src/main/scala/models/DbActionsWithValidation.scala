package models

import models.DbActions._
import scala.concurrent.Future

object DbActionsWithValidation {

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

  private def validatePaginationTopics(limit: Int, offset: Int) = {
    ""
  }

  private def validatePaginationAnswers(before: Int, after: Int) = {
    ""
  }

  private def validateUserParams(user: User) = {
    user.nick.length > 2 && user.nick.length < 20 &&
    user.email.contains('@') && user.email.length > 5 &&
    user.email.length < 30
  }

  private def validateContent(contents: Contents) = contents match {
    case t: Topic => t.subject.length > 1 && t.subject.length < 50 &&
     t.content.length > 3 && t.content.length < 1000
    case a: Answer => true
  }

}
