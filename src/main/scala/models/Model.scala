package models

trait Contents

case class User(nick: String, email: String) {
  def toTuple = (nick, email)
}

case class Topic(user: User, subject: String, content: String) extends Contents {
  def toTuple = (user.toTuple, subject, content)
}

case class Answer(user: User, content: String) extends Contents {
  def toTuple = (user.toTuple, content)
}

trait Response

case class Success(msg: String) extends Response

case class Failure(msg: String) extends Response

object SortType extends Enumeration {
  def parse(s: String): Value = values.find(_.toString.equalsIgnoreCase(s)).getOrElse(Latest)
  type SortType = Value
  val Popular, Latest = Value
}

object ContentType extends Enumeration {
  type ContentType = Value
  val Topics, Answers = Value
}

case class User_db(id: Int, nick: String, email: String)

case class Topic_db(id: Int = 0, nickname: String = "", timestamp: String = "", subject: String = "", content: String = "")

case class Answer_db(id: Int, nickname: String, timestamp: String, content: String)

case class Popular_db(id: Int, subject: String, a_time: String, nickname: String, timestamp: String)