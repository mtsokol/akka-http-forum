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