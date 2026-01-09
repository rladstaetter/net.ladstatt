package net.ladstatt.app

case class AppId(name: String
                 , id : String
                 , groupId: String) {

  require(name.nonEmpty, "name must not be empty")
  require(id.nonEmpty, "id must not be empty")
  require(groupId.nonEmpty, "groupId must not be empty")

}

