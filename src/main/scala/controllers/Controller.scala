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
      answers => DbActions.getTopic(topicID).map {
          case IndexedSeq() => None
          case topic => Some((answers, topic))
        }
    }
  }

  def createTopic(topic: Topic) = {
    DbActions.checkUser(topic.user).flatMap {
      case IndexedSeq() => DbActions.createUser(topic.user).flatMap {
        case id => DbActions.createTopic(topic, id)
      }
      case i +: _ => DbActions.createTopic(topic, i._1)
    }
  }

  def createAnswer(answer: Answer, topicID: Int) = {
    DbActions.checkUser(answer.user).flatMap {
      case IndexedSeq() => DbActions.createUser(answer.user).flatMap {
        case id => DbActions.createAnswer(answer, topicID, id)
      }
      case i +: _ => DbActions.createAnswer(answer, topicID, i._1)
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
