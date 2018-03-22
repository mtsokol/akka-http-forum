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

  val users = TableQuery[Users]

  class Topics(tag: Tag) extends Table[(Int, Int, String, String, String)](tag, "topics") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userid = column[Int]("user_id")

    def timestamp = column[String]("timestamp")

    def secret = column[String]("secret")

    def comment = column[String]("content")

    def * = (id, userid, timestamp, secret, comment)

    def supplier = foreignKey("topics_users_id_fk", id, users)(_.id)
  }

  val topics = TableQuery[Topics]

  class Answers(tag: Tag) extends Table[(Int, Int, Int, String, String, String)](tag, "answers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userid = column[Int]("user_id")

    def topicid = column[Int]("topic_id")

    def timestamp = column[String]("timestamp")

    def secret = column[String]("secret")

    def comment = column[String]("content")

    def * = (id, userid, topicid, timestamp, secret, comment)

    def supplier = foreignKey("answers_users_id_fk", id, users)(_.id)

    def supplier2 = foreignKey("answers_topics_id_fk", id, topics)(_.id)
  }

  val Answers = TableQuery[Answers]

  val db = Database.forConfig("dbconnection")
}
