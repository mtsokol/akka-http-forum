package controllers

import models.{DbActions, Topic, User}
import models.DbScheme._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

object Controller {

  def getTopics(sort: String, limit: Int, offset: Int) = {
    DbActions.getTopics(sort, limit, offset)
  }

  def getTopic() = {

  }

  def createTopic() = {

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
