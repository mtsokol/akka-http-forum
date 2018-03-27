package models

import models.DbScheme._
import slick.jdbc.PostgresProfile.api._

object DbActions {

  def getTopics(sort: String, limit: Int, offset: Int) = { //TODO sorting
    val action = for {
      (t, u) <- TopicsTable join UsersTable on (_.userid === _.id)
    } yield (t.id, t.timestamp, t.subject, u.nickname)

    val action2 = action.drop(offset).take(limit).result

    db.run(action2)
  }

  def checkUser(user: User) = {
    val action = UsersTable.filter(u => u.nickname === user.nick
      && u.email === user.email).result
    db.run(action)
  }

  def createUser(user: User) = {
    val insertActions =
      UsersTable.map(x=>(x.nickname, x.email)).returning(UsersTable.map(_.id)) += user.toTuple

    db.run(insertActions)
  }

  def getTopic(topicID: Int) = {
    val action = for {
      (t, u) <- TopicsTable join UsersTable on (_.userid === _.id) if t.id === topicID
    } yield (t.timestamp, t.content, u.nickname)

    db.run(action.result)
  }

  def getAnswers(id: Int, mid: Int, before: Int, after: Int) = {
    val action = AnswersTable.filter(_.topicid === id).sortBy(_.id)
      .drop(mid-before).take(after).result
    db.run(action)
  }

  def createTopic(topic: Topic, userID: Int) = {
    val insertAction = DBIO.seq(
      TopicsTable.map(x => (x.userid, x.secret, x.subject, x.content))
        += (userID, SecretGenerator.generateSecret(), topic.subject, topic.content)
    )
    db.run(insertAction)
  }

  def createAnswer(answer: Answer, topicID: Int, userID: Int) = {
    val insertAction = DBIO.seq(
      AnswersTable.map(x => (x.userid, x.topicid, x.secret, x.content))
        += (userID, topicID, SecretGenerator.generateSecret(), answer.content)
    )
    db.run(insertAction)
  }

  def modifyTopic(topicID: Int, newContent: String) = {
    val q = for { t <- TopicsTable if t.id === topicID } yield t.content
    val updateAction = q.update(newContent)
    db.run(updateAction)
  }

  def modifyAnswer(answerID: Int, newContent: String) = {
    val q = for { t <- AnswersTable if t.id === answerID } yield t.content
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

  def validateSecret(kind: String, id: Int, secret: String) = {
    val q = kind match {
      case "Answer" => AnswersTable.filter(x => x.id === id && x.secret === secret)
      case "Topic" => TopicsTable.filter(x => x.id === id && x.secret === secret)
    }
    val action = q.result

    db.run(action)
  }

}
