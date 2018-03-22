package controllers

import models.DbScheme._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object Controller {

  def basicFun(ids: Int) = {
    //try {
      db.run(users.result).map(i =>
        i.map{ case (x,y,z) => x})
    //} finally db.close
  }

}
