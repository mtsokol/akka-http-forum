package controllers

import models._
import models.ContentType._
import models.SortType._
import akka.http.scaladsl.server.Directives
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.DbActionsWithValidation._

object Controller extends Directives {

  def getTopics(sort: SortType, limit: Int, offset: Int) = {
    getTopicsWithValidation(sort, limit, offset)
  }

  def getTopic(topicID: Int, mid: Int, before: Int, after: Int) = {
    getAnswersWithValidation(topicID, mid, before, after).flatMap {
      answers =>
        DbActions.getTopic(topicID).map {
          case IndexedSeq() => None
          case topic => Some((answers, topic))
        }
    }
  }

  def createTopic(topic: Topic): Future[Response] = {
    DbActions.checkUser(topic.user).flatMap {
      case IndexedSeq() => createUserWithValidation(topic.user).fold(
        Future(Failure("Invalid user params").asInstanceOf[Response])
      ) { eventualInt =>
        eventualInt.flatMap(id => createTopicWithValidation(topic, id).fold(
          Future(Failure("Invalid topic params").asInstanceOf[Response])
        ) {
          eventualResponse => eventualResponse
        })
      }
      case user +: _ => createTopicWithValidation(topic, user.id).fold(
        Future(Failure("Invalid topic params").asInstanceOf[Response])
      ) {
        eventualResponse => eventualResponse
      }
    }
  }

  def createAnswer(answer: Answer, topicID: Int): Future[Response] = {
    DbActions.checkUser(answer.user).flatMap {
      case IndexedSeq() => createUserWithValidation(answer.user).fold(
        Future(Failure("Invalid user params").asInstanceOf[Response])
      ) { eventualInt =>
        eventualInt.flatMap(id => createAnswerWithValidation(answer, topicID, id).fold(
          Future(Failure("Invalid answer params").asInstanceOf[Response])
        ) {
          eventualResponse => eventualResponse
        })
      }
      case user +: _ => createAnswerWithValidation(answer, topicID, user.id).fold(
        Future(Failure("Invalid answer params").asInstanceOf[Response])
      ) {
        eventualResponse => eventualResponse
      }
    }
  }

  def modifyTopic(topicID: Int, secret: String, newContent: String): Future[Option[Int]] = {
    DbActions.validateSecret(Topics, topicID, secret).flatMap {
      case _ +: _ => DbActions.modifyTopic(topicID, newContent).map {
        stat: Int => Some(stat)
      }
      case IndexedSeq() => Future {
        None
      }
    }
  }

  def modifyAnswer(answerID: Int, secret: String, newContent: String): Future[Option[Int]] = {
    DbActions.validateSecret(Answers, answerID, secret).flatMap {
      case _ +: _ => for {
        result <- DbActions.modifyAnswer(answerID, newContent)
      } yield Some(result)
      case IndexedSeq() => Future {
        None
      }
    }
  }

  def deleteTopic(topicID: Int, secret: String): Future[Object] = {
    DbActions.validateSecret(Topics, topicID, secret).flatMap {
      case _ +: _ => for {
        result <- DbActions.deleteTopic(topicID)
      } yield Some(result)
      case IndexedSeq() => Future {
        None
      }
    }
  }

  def deleteAnswer(answerID: Int, secret: String): Future[Object] = {
    DbActions.validateSecret(Answers, answerID, secret).flatMap {
      case _ +: _ => for {
        result <- DbActions.deleteAnswer(answerID)
      } yield Some(result)
      case IndexedSeq() => Future {
        None
      }
    }
  }

}
