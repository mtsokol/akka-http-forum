package models

import models.DbScheme._
import models.SortType._
import models.ContentType._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

object DbActions {

  def getTopics(sort: SortType, limit: Int, offset: Int): Future[Seq[Topic]] = {
    sort match {
      case Latest =>
        val action = for {
          (topics, users) <- TopicsTable join UsersTable on (_.userid === _.id)
        } yield (topics.id, topics.timestamp, users.nickname, topics.subject)
        val action2 = action.sortBy(_._2.desc).drop(offset).take(limit).result
        db.run(action2.map(_.map(tuple => Topic(tuple._1, tuple._2, tuple._3, tuple._4))))
      case Popular =>
        val action = PopularView.drop(offset).take(limit)
          .map(record => (record.id, record.timestamp, record.nickname, record.subject)).result
        db.run(action.map(_.map(tuple => Topic(tuple._1, tuple._2, tuple._3, tuple._4))))
      case _ => Future {
        Seq()
      }
    }
  }

  def checkUser(user: UserInput): Future[Seq[User]] = {
    val action = UsersTable.filter(u => u.nickname === user.nick
      && u.email === user.email).result
    db.run(action.map(_.map(user => User(user._1, user._2, user._3))))
  }

  def createUser(user: UserInput): Future[Int] = {
    val insertActions =
      UsersTable.map(users => (users.nickname, users.email)).returning(UsersTable.map(_.id)) += user.toTuple

    db.run(insertActions)
  }

  def getTopic(topicID: Int): Future[Seq[TopicWithContent]] = {
    val action = for {
      (topics, users) <- TopicsTable join UsersTable on (_.userid === _.id) if topics.id === topicID
    } yield (topics.subject, topics.content, topics.timestamp, users.nickname)

    db.run(action.result.map(_.map(tuple => TopicWithContent(tuple._1, tuple._2, tuple._3, tuple._4))))
  }

  def getAnswers(id: Int, mid: Int, before: Int, after: Int): Future[Seq[Answer]] = {
    val action = for {
      (answers, users) <- AnswersTable join UsersTable on (_.userid === _.id) if answers.topicid === id
    } yield (answers.id, answers.timestamp, users.nickname, answers.content)
    db.run(action.sortBy(_._1).drop(mid - before).take(after)
      .result.map(_.map(tuple => Answer(tuple._1, tuple._2, tuple._3, tuple._4))))
  }

  def createTopic(topic: TopicInput, userID: Int): Future[Try[String]] = {
    val secret = SecretGenerator.getSecret
    val insertAction = DBIO.seq(
      TopicsTable.map(topics => (topics.userid, topics.secret, topics.subject, topics.content))
        += (userID, secret, topic.subject, topic.content)
    )
    db.run(insertAction).map(_ => Success(secret))
  }

  def createAnswer(answer: AnswerInput, topicID: Int, userID: Int): Future[Try[String]] = {
    val secret = SecretGenerator.getSecret
    val insertAction = DBIO.seq(
      AnswersTable.map(answers => (answers.userid, answers.topicid, answers.secret, answers.content))
        += (userID, topicID, secret, answer.content)
    )
    db.run(insertAction).map(_ => Success(secret))
  }

  def modifyTopic(topicID: Int, newContent: String): Future[Int] = {
    val query = for {t <- TopicsTable if t.id === topicID} yield t.content
    val updateAction = query.update(newContent)
    db.run(updateAction)
  }

  def modifyAnswer(answerID: Int, newContent: String): Future[Int] = {
    val query = for {t <- AnswersTable if t.id === answerID} yield t.content
    val updateAction = query.update(newContent)
    db.run(updateAction)
  }

  def deleteTopic(topicID: Int): Future[Int] = {
    val query = TopicsTable.filter(_.id === topicID)
    val action = query.delete
    db.run(action)
  }

  def deleteAnswer(answerID: Int): Future[Int] = {
    val query = AnswersTable.filter(_.id === answerID)
    val action = query.delete
    db.run(action)
  }

  def validateSecret(kind: ContentType, id: Int, secret: String) = {
    val query = kind match {
      case Answers => AnswersTable.filter(x => x.id === id && x.secret === secret)
      case Topics => TopicsTable.filter(x => x.id === id && x.secret === secret)
    }
    val action = query.result

    db.run(action)
  }

}
