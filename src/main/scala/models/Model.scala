package models

case class User(ID: Int, nick: String, email: String)

case class Topic(ID: Int, userID: Int, timestamp: String,
                 secret: String, content: String)

case class Answer(ID: Int, userID: Int, topicID: Int,
                        timestamp: String, secret: String, content: String)
