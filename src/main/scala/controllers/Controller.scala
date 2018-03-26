package controllers

import models.{Answer, DbActions, Topic}
import akka.http.scaladsl.server.Directives
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Controller extends Directives {

  def getTopics(sort: String, limit: Int, offset: Int) = {
    DbActions.getTopics(sort, limit, offset)
  }

  def getTopic(topicID: Int, mid: Int, before: Int, after: Int) = {
    DbActions.getAnswers(topicID, mid, before, after).flatMap {
      case e => DbActions.getTopic(topicID).map {
        case a => (e,a)
      }
    }
  }

  def createTopic(topic: Topic) = {
    DbActions.createTopic(topic)
  }

  def createAnswer(answer: Answer) = {
    DbActions.createAnswer(answer)
  }

  def modifyTopic(topic: Topic) = {
    DbActions.validateSecret("Answer", topic.ID, topic.secret).flatMap {
      case i +: rest => DbActions.modifyTopic(topic).map {
        stat: Int => Some(stat)
      }
      case IndexedSeq() => Future { None }
    }
  }

  def modifyAnswer(answer: Answer): Future[Option[Int]] = {
    DbActions.validateSecret("Answer", answer.ID, answer.secret).flatMap {
      case e +: _ => for {
        result <- DbActions.modifyAnswer(answer)
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
