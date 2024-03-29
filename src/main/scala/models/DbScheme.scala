package models

import slick.jdbc.PostgresProfile.api._

/**
  * Objective representation of DataBase model
  *
  * @see postgres
  */
object DbScheme {

  class Users(tag: Tag) extends Table[(Int, String, String)](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def nickname = column[String]("nickname")

    def email = column[String]("email")

    def * = (id, nickname, email)
  }

  val UsersTable = TableQuery[Users]

  class Topics(tag: Tag) extends Table[(Int, Int, String, String, String, String)](tag, "topics") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userid = column[Int]("user_id")

    def timestamp = column[String]("timestamp")

    def secret = column[String]("secret")

    def subject = column[String]("subject")

    def content = column[String]("content")

    def * = (id, userid, timestamp, secret, subject, content)

    def supplier = foreignKey("topics_users_id_fk", id, UsersTable)(_.id, onDelete=ForeignKeyAction.Cascade)
  }

  val TopicsTable = TableQuery[Topics]

  class Answers(tag: Tag) extends Table[(Int, Int, Int, String, String, String)](tag, "answers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userid = column[Int]("user_id")

    def topicid = column[Int]("topic_id")

    def timestamp = column[String]("timestamp")

    def secret = column[String]("secret")

    def content = column[String]("content")

    def * = (id, userid, topicid, timestamp, secret, content)

    def supplier = foreignKey("answers_users_id_fk", id, UsersTable)(_.id, onDelete=ForeignKeyAction.Cascade)

    def supplier2 = foreignKey("answers_topics_id_fk", id, TopicsTable)(_.id, onDelete=ForeignKeyAction.Cascade)
  }

  val AnswersTable = TableQuery[Answers]

  class Popular(tag: Tag) extends Table[(Int, String, String, String, String)](tag,"popular") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def subject = column[String]("subject")

    def a_time = column[String]("timestamp")

    def nickname = column[String]("nickname")

    def timestamp = column[String]("timestamp")

    def * = (id, subject, a_time, nickname, timestamp)
  }

  val PopularView = TableQuery[Popular]

  val db = Database.forConfig("dbconnection")


}
