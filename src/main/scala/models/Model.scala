package models

trait InputContents

case class User(nick: String, email: String) {
  def toTuple = (nick, email)
}

case class Topic(user: User, subject: String, content: String) extends InputContents {
  def toTuple = (user.toTuple, subject, content)
}

case class Answer(user: User, content: String) extends InputContents {
  def toTuple = (user.toTuple, content)
}

object SortType extends Enumeration {
  def parse(str: String): Value = values.find(_.toString.equalsIgnoreCase(str)).getOrElse(Latest)
  type SortType = Value
  val Popular, Latest = Value
}

object ContentType extends Enumeration {
  type ContentType = Value
  val Topics, Answers = Value
}

trait OutputContents

case class User_db(id: Int, nick: String, email: String) extends  OutputContents

case class Topic_db(id: Int, timestamp: String, nickname: String, subject: String) extends  OutputContents

case class Answer_db(id: Int, timestamp: String, nickname: String,  content: String) extends  OutputContents

case class Topic_with_content_db(subject: String, content: String, timestamp: String, nickname: String) extends  OutputContents