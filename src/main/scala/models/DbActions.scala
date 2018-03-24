package models

import models.DbScheme._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object DbActions {

  def getTopics(sort: String, limit: Int, offset: Int) = {
    val action = for {
      (t, u) <- TopicsTable join UsersTable on (_.userid === _.id)
    } yield (t.timestamp, t.subject, u.nickname)

    val action2 = action.drop(offset).take(limit).result

    db.run(action2)
  }

  def checkUser(user: User) = {
    val insertActions = DBIO.seq(
      UsersTable += user.toTuple
    )
    UsersTable.insertStatement
  }

  def createUser(user: User) = {
    val insertActions = DBIO.seq(
      UsersTable += user.toTuple
    )
    UsersTable.insertStatement
  }

  def getTopic(id: Int) = {
    val action = for {
      (t, u) <- TopicsTable join UsersTable on (_.userid === _.id) if t.id === id
    } yield (t.timestamp, t.content, u.nickname)

    db.run(action.result)
  }

  def getAnswers(id: Int, mid: Int, before: Int, after: Int) = {
    val action = AnswersTable.filter(_.topicid === id).sortBy(_.id)
      .drop(mid-before).take(after).result
    db.run(action)
  }

  def createTopic(topic: Topic) = {
    val insertAction = DBIO.seq(
      TopicsTable.map(x => (x.userid, x.secret, x.subject, x.content))
        += (topic.userID, topic.secret, topic.subject, topic.content)
    )
    db.run(insertAction)
  }

  def createAnswer(user: User, answer: Answer) = {

  }

  def modifyTopic(id: Int, newContent: Topic, secret: String) = {
    val q = for { t <- TopicsTable if t.id === id } yield t
    val updateAction = q.update(newContent.toTuple)

    val sql = q.updateStatement
  }

  def modifyAnswer(id: Int, newContent: Answer, secret: String) = {
    val q = for { t <- AnswersTable if t.id === id } yield t
    val updateAction = q.update(newContent.toTuple)

    val sql = q.updateStatement
  }

  def deleteTopic(id: Int, secret: String) = {
    val q = TopicsTable.filter(_.id === id)
    val action = q.delete
    db.run(action)
  }

  def deleteAnswer(id: Int, secret: String) = {
    val q = AnswersTable.filter(_.id === id)
    val action = q.delete
    db.run(action)
  }

}
