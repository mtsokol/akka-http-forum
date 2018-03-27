package models

case class User(nick: String, email: String) {
  def toTuple = (nick, email)
}

case class Topic(user: User, subject: String, content: String) {
  def toTuple = (user.toTuple, subject, content)
}

case class Answer(user: User, content: String) {
  def toTuple = (user.toTuple, content)
}
