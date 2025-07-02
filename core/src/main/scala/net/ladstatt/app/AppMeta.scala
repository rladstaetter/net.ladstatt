package net.ladstatt.app

object AppMeta {

  val LogFormat = """[%1$tF %1$tT.%1$tN] %3$-40s %4$-13s %5$s %6$s %n"""

  def initApp(appMeta: AppMeta) : Unit = {
    System.setProperty("app.name", appMeta.appId.name)
    System.setProperty("app.id", appMeta.appId.id)
    System.setProperty("app.groupId", appMeta.appId.groupId)
    System.setProperty("java.util.logging.SimpleFormatter.format", appMeta.logFormat)
  }

}
case class AppMeta(appId: AppId
                   , logFormat: String)
