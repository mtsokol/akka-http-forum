package models

import models.DbScheme._
import models.SortType._
import models.ContentType._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

object DbActions {

  def getTopics(sort: SortType, limit: Int, offset: Int): Future[Seq[Topic_db]] = {
    sort match {
      case Latest =>
        val action = for {
          (t, u) <- TopicsTable join UsersTable on (_.userid === _.id)
        } yield (t.id, t.timestamp, u.nickname, t.subject)
        val action2 = action.sortBy(_._2.desc).drop(offset).take(limit).result
        db.run(action2.map(_.map(y => Topic_db(y._1, y._2, y._3, y._4))))
      case Popular =>
        val action = PopularView.drop(offset).take(limit).map(x => (x.id, x.timestamp, x.nickname, x.subject)).result
        db.run(action.map(_.map(y => Topic_db(y._1, y._2, y._3, y._4))))
      case _ => Future {
        Seq()
      }
    }
  }

  def checkUser(user: User): Future[Seq[User_db]] = {
    val action = UsersTable.filter(u => u.nickname === user.nick
      && u.email === user.email).result
    db.run(action.map(_.map(y => User_db(y._1, y._2, y._3))))
  }

  def createUser(user: User): Future[Int] = {
    val insertActions =
      UsersTable.map(x => (x.nickname, x.email)).returning(UsersTable.map(_.id)) += user.toTuple

    db.run(insertActions)
  }

  def getTopic(topicID: Int): Future[Seq[Topic_with_content_db]] = {
    val action = for {
      (t, u) <- TopicsTable join UsersTable on (_.userid === _.id) if t.id === topicID
    } yield (t.subject, t.content, t.timestamp, u.nickname)

    db.run(action.result.map(_.map(y => Topic_with_content_db(y._1, y._2, y._3, y._4))))
  }

  def getAnswers(id: Int, mid: Int, before: Int, after: Int): Future[Seq[Answer_db]] = {
    val action = for {
      (a, u) <- AnswersTable join UsersTable on (_.userid === _.id) if a.topicid === id
    } yield (a.id, a.timestamp, u.nickname, a.content)
    db.run(action.sortBy(_._1).drop(mid - before).take(after).result.map(_.map(y => Answer_db(y._1, y._2, y._3, y._4))))
  }

  def createTopic(topic: Topic, userID: Int): Future[Try[String]] = {
    val secret = SecretGenerator.getSecret
    val insertAction = DBIO.seq(
      TopicsTable.map(x => (x.userid, x.secret, x.subject, x.content))
        += (userID, secret, topic.subject, topic.content)
    )
    db.run(insertAction).map(_ => Success(secret))
  }

  def createAnswer(answer: Answer, topicID: Int, userID: Int): Future[Try[String]] = {
    val secret = SecretGenerator.getSecret
    val insertAction = DBIO.seq(
      AnswersTable.map(x => (x.userid, x.topicid, x.secret, x.content))
        += (userID, topicID, secret, answer.content)
    )
    db.run(insertAction).map(_ => Success(secret))
  }

  def modifyTopic(topicID: Int, newContent: String) = {
    val q = for {t <- TopicsTable if t.id === topicID} yield t.content
    val updateAction = q.update(newContent)
    db.run(updateAction)
  }

  def modifyAnswer(answerID: Int, newContent: String) = {
    val q = for {t <- AnswersTable if t.id === answerID} yield t.content
    val updateAction = q.update(newContent)
    db.run(updateAction)
  }

  def deleteTopic(topicID: Int) = {
    val q = TopicsTable.filter(_.id === topicID)
    val action = q.delete
    db.run(action)
  }

  def deleteAnswer(answerID: Int) = {
    val q = AnswersTable.filter(_.id === answerID)
    val action = q.delete
    db.run(action)
  }

  def validateSecret(kind: ContentType, id: Int, secret: String) = {
    val q = kind match {
      case Answers => AnswersTable.filter(x => x.id === id && x.secret === secret)
      case Topics => TopicsTable.filter(x => x.id === id && x.secret === secret)
    }
    val action = q.result

    db.run(action)
  }

}
