package net.ladstatt

import net.ladstatt.util.log.TinyLog

import java.nio.file.Paths


object MyAppTest extends TinyLog {

  def main(args: Array[String]): Unit = {
    TinyLog.init(Paths.get("target/simplelog.log"))
    logTrace("trace")
    logConfig("config")
    logError("severe")
    logWarn("warn")
    logInfo("info")
    logException("exception", new Throwable("java.lang.Exception"))
  }

}


