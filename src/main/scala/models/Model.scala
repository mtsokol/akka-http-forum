package models

trait InputContents

case class UserInput(nick: String, email: String) {
  def toTuple = (nick, email)
}

case class TopicInput(user: UserInput, subject: String, content: String) extends InputContents

case class AnswerInput(user: UserInput, content: String) extends InputContents

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

case class User(id: Int, nick: String, email: String) extends  OutputContents

case class Topic(id: Int, timestamp: String, nickname: String, subject: String) extends  OutputContents

case class Answer(id: Int, timestamp: String, nickname: String, content: String) extends  OutputContents

case class TopicWithContent(subject: String, content: String, timestamp: String, nickname: String) extends  OutputContents
