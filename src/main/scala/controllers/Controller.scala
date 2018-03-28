package controllers

import models._
import akka.http.scaladsl.server.Directives
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.DbActionsWithValidation._

object Controller extends Directives {

  def getTopics(sort: String, limit: Int, offset: Int) = {
    DbActions.getTopics(sort, limit, offset)
  }

  def getTopic(topicID: Int, mid: Int, before: Int, after: Int) = {
    DbActions.getAnswers(topicID, mid, before, after).flatMap {
      answers => DbActions.getTopic(topicID).map {
          case IndexedSeq() => None
          case topic => Some((answers, topic))
        }
    }
  }

  def createTopic(topic: Topic): Future[Response] = {
    DbActions.checkUser(topic.user).flatMap {
      case IndexedSeq() => createUserWithValidation(topic.user).fold(
          Future(Failure("Invalid user params").asInstanceOf[Response])
      ){ x =>
        x.flatMap(id => createTopicWithValidation(topic, id).fold(
          Future(Failure("Invalid topic params").asInstanceOf[Response])
        ){ y => y
        })
      }
      case user +: _ => createTopicWithValidation(topic, user._1).fold(
        Future(Failure("Invalid topic params").asInstanceOf[Response])
      ) {
        y => y
      }
    }
  }

  def createAnswer(answer: Answer, topicID: Int): Future[Response] = {
    DbActions.checkUser(answer.user).flatMap {
      case IndexedSeq() => createUserWithValidation(answer.user).fold(
        Future(Failure("Invalid user params").asInstanceOf[Response])
      ){ x =>
        x.flatMap(id => createAnswerWithValidation(answer, topicID, id).fold(
          Future(Failure("Invalid answer params").asInstanceOf[Response])
        ){ y => y
        })
      }
      case i +: _ => createAnswerWithValidation(answer, topicID, i._1).fold(
        Future(Failure("Invalid answer params").asInstanceOf[Response])
      ) {
        y => y
      }
    }
  }

  def modifyTopic(topicID: Int, secret: String, newContent: String) = {
    DbActions.validateSecret("Topic", topicID, secret).flatMap {
      case i +: rest => DbActions.modifyTopic(topicID, newContent).map {
        stat: Int => Some(stat)
      }
      case IndexedSeq() => Future { None }
    }
  }

  def modifyAnswer(answerID: Int, secret: String, newContent: String): Future[Option[Int]] = {
    DbActions.validateSecret("Answer", answerID, secret).flatMap {
      case e +: _ => for {
        result <- DbActions.modifyAnswer(answerID, newContent)
      } yield Some(result)
      case IndexedSeq() => Future { None }
    }
  }

  def deleteTopic(topicID: Int, secret: String): Future[Object] = {
    DbActions.validateSecret("Topic", topicID, secret).flatMap {
      case e +: _ => for {
        result <- DbActions.deleteTopic(topicID)
      } yield Some(result)
      case IndexedSeq() => Future { None }
    }
  }

  def deleteAnswer(answerID: Int, secret: String): Future[Object] = {
    DbActions.validateSecret("Answer", answerID, secret).flatMap {
      case e +: _ => for {
        result <- DbActions.deleteAnswer(answerID)
      } yield Some(result)
      case IndexedSeq() => Future { None }
    }
  }

}
