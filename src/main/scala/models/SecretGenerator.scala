package models

import scala.util.Random

object SecretGenerator {

  def getSecret: String = {
    Random.alphanumeric take 7 mkString
  }

}
