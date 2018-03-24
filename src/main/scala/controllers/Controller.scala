package controllers

import models.{DbActions, Topic, User}

object Controller {

  def getTopics(sort: String, limit: Int, offset: Int) = {
    DbActions.getTopics(sort, limit, offset)
  }

  def getTopic() = {

  }

  def createTopic(topic: Topic) = {
    DbActions.createTopic(topic)
  }

  def createAnswer() = {

  }

  def modifyTopic() = {

  }

  def modifyAnswer() = {

  }

  def deleteTopic() = {

  }

  def deleteAnswer() = {

  }

}
