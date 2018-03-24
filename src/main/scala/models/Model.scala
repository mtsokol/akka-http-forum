package models

case class User(ID: Int, nick: String, email: String) {
  def toTuple = (ID, nick, email)
}

case class Topic(ID: Int, userID: Int, timestamp: String, secret: String, subject: String,
                 content: String) {
  def toTuple = (ID, userID, timestamp, secret, subject, content)
}

case class Answer(ID: Int, userID: Int, topicID: Int, timestamp: String, secret: String,
                  content: String) {
  def toTuple = (ID, userID, topicID, timestamp, secret, content)
}
