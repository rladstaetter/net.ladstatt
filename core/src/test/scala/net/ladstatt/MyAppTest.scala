package net.ladstatt

import net.ladstatt.app.{AppId, AppMeta}
import net.ladstatt.util.log.CanLog

object MyAppTest extends CanLog {

  val appMeta: AppMeta = AppMeta(AppId("MyApp", "myapp", "my.app"), AppMeta.LogFormat)

  def main(args: Array[String]): Unit = {
    AppMeta.initApp(appMeta)
    logTrace("trace")
    logWarn("warn")
    logInfo("info")
    logException("exception", new Throwable("java.lang.Exception"))
  }

}


