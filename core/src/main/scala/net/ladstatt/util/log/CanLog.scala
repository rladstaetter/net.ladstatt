package net.ladstatt.util.log

import net.ladstatt.app.AppId
import net.ladstatt.util.os.OsUtil
import net.ladstatt.util.os.OsUtil._

import java.io.{PrintWriter, StringWriter}
import java.nio.file.{Files, Path, Paths}
import java.util.logging._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object CanLog {

  var someStreamHandler: Option[StreamHandler] = None

  def mkLogHandler(logFilePath: Path, logLevel: Level = Level.INFO, appendLogs: Boolean = false): StreamHandler = {
    someStreamHandler match {
      case Some(value) => value
      case None =>
        val h = if (!Files.exists(logFilePath.getParent)) {
          Try(Files.createDirectories(logFilePath.getParent)) match {
            case Success(_) => mkFileHandler(logFilePath, appendLogs)
            case Failure(_) => new ConsoleHandler()
          }
        } else {
          Try {
            mkFileHandler(logFilePath, appendLogs)
          } match {
            case Success(h) => h
            case Failure(e) =>
              e.printStackTrace()
              new ConsoleHandler()
          }

        }
        h.setLevel(logLevel)
        h.setFormatter(new SimpleFormatter)
        someStreamHandler = Option(h)
        h
    }

  }

  private def mkFileHandler(logFilePath: Path, appendLogs: Boolean) = {
    new FileHandler(logFilePath.toAbsolutePath.toString, appendLogs)
  }

}


trait CanLog extends BasicLogMethods {

  def appId: AppId = {
    AppId(
      System.getProperty("app.name")
      , System.getProperty("app.groupId")
      , System.getProperty("app.id"))
  }

  lazy val pathMap: Map[Os, Path] =
    Map(Windows -> Paths.get(s"C:/ProgramData/${appId.id}/")
      , Mac -> Paths.get(System.getProperty("user.home")).resolve(s"Library/Application Support/${appId.id}/")
      , Linux -> Paths.get(System.getProperty("user.home")).resolve(s".${appId.id}/")
      , LinuxFlatPak -> Option(System.getenv("XDG_CONFIG_HOME")).map(p => Paths.get(p)).orNull
    )

  lazy val logPathMap: Map[Os, Path] =
    pathMap ++ Map(LinuxFlatPak -> Option(System.getenv("XDG_DATA_HOME")).map(p => Paths.get(p)).orNull)

  lazy val logHandler: Handler = CanLog.mkLogHandler(logFilePath, logLevel, appendLogs)

  lazy val log: Logger = {
    val lggr = Logger.getLogger(this.getClass.getName)
    lggr.setLevel(logLevel)
    lggr.addHandler(logHandler)
    lggr
  }

  def logFilePath: Path = logPathMap(OsUtil.currentOs).resolve(appId.id + ".log")

  def settingsFilePath: Path = pathMap(OsUtil.currentOs).resolve(appId.id + ".conf")

  def logLevel: Level = Level.INFO

  def appendLogs: Boolean = false

  private def throwToString(t: Throwable): String = {
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    try {
      t.printStackTrace(pw)
      sw.toString // stack trace as a string
    } finally {
      Option(sw).foreach(_.close())
      Option(pw).foreach(_.close())
    }
  }

  def logInfo(msg: String): Unit = Try(log.info(msg))

  def logWarn(msg: String): Unit = Try(log.warning(msg))

  def logTrace(msg: String): Unit = Try(log.finest(msg))

  def logException(msg: String, t: Throwable): Unit = {

    val R = "\r"
    val N = "\n"
    val RN = s"$R$N"
    logError(msg)
    Option(t).foreach {
      t =>
        for (l <- throwToString(t).split(N)) {
          val msg = l.replaceAll(R, "").replaceAll(RN, "").replaceAll(N, "")
          if (msg.nonEmpty) {
            log.severe(msg)
          }
        }
    }
  }

  def logError(msg: String): Unit = log.severe(msg)

  /**
   * If execution time of function 'a' exceeds errorThreshold, an error log message is written, otherwise a trace log
   *
   * @param a             action to perform
   * @param msg           message to decorate a in the log
   * @param warnThreshold max duration for an operation until a 'exceeded time' log message is issued
   * @tparam A type of result
   * @return
   */
  def timeR[A](a: => A
               , msg: String
               , warnThreshold: FiniteDuration = 500 millis
               , log: String => Unit = logTrace): A = {
    val now = System.nanoTime
    val result = a
    val millis = (System.nanoTime - now) / (1000 * 1000)
    if (millis <= warnThreshold.toMillis) {
      log(s"$msg (duration: $millis ms)")
    } else {
      logWarn(s"$msg (duration: $millis ms [LONG OPERATION])")
    }
    result
  }

}
