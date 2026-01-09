package net.ladstatt.util.log

import java.io.{PrintWriter, StringWriter}
import java.nio.file.{Files, Path}
import java.util.logging.{ConsoleHandler, FileHandler, Handler, Level, Logger, SimpleFormatter}

/**
 * Provides one logger for one application.
 *
 * Initialize the logger once per process via TinyLog.init(p : Path).
 *
 * Use the `TinyLog` trait in your classes, log using logInfo, logWarn ...
 */
object TinyLog {

  private val SharedLoggerName = "net.ladstatt.app"
  private val DefaultFormat = "[%1$tF %1$tT.%1$tN] %2$-40s %4$-13s %5$s %6$s %n"

  // 10 MB limit per file
  private val DefaultLimit = 10 * 1024 * 1024
  // Keep 10 old log files
  private val DefaultCount = 10

  var sharedHandler: Option[Handler] = None

  lazy val sharedLogger: Logger = {
    val logger = Logger.getLogger(SharedLoggerName)
    logger.setUseParentHandlers(false)
    logger.setLevel(Level.ALL)
    logger
  }

  /**
   * @param limit max bytes per file (e.g., 10485760 for 10MB)
   * @param count number of files to cycle through
   */
  def init(logFilePath: Path,
           appendLogs: Boolean = true, // Usually true when rolling
           limit: Int = DefaultLimit,
           count: Int = DefaultCount,
           format: String = DefaultFormat): Unit = {

    if (sharedHandler.isEmpty) {
      System.setProperty("java.util.logging.SimpleFormatter.format", format)

      if (!Files.exists(logFilePath.getParent)) {
        Files.createDirectories(logFilePath.getParent)
      }

      // FileHandler(pattern, limit, count, append)
      // The pattern should usually stay as the absolute path string.
      // JUL will automatically append .0, .1, .2 to the filename when rolling.
      val handler = new FileHandler(
        logFilePath.toAbsolutePath.toString,
        limit,
        count,
        appendLogs
      )

      handler.setFormatter(new SimpleFormatter)
      handler.setLevel(Level.ALL)

      sharedLogger.addHandler(handler)

      // Add ConsoleHandler so you see logs in stdout too
      val console = new ConsoleHandler()
      console.setLevel(Level.ALL)
      sharedLogger.addHandler(console)

      sharedHandler = Some(handler)
    }
  }
}

/**
 * Mix in this trait in your classes if you want to log
 */
trait TinyLog {

  private val logger: Logger = TinyLog.sharedLogger
  private val className: String = this.getClass.getName

  private def doLog(level: Level, msg: String, t: Throwable = null): Unit = {
    // logp(level, sourceClass, sourceMethod, msg, params/throwable)
    // We pass className to sourceClass so it appears in the %3 slot of your format
    if (t == null) {
      logger.logp(level, className, "", msg)
    } else {
      logger.logp(level, className, "", msg, t)
    }
  }

  def logConfig(msg: String): Unit = doLog(Level.CONFIG, msg)

  def logInfo(msg: String): Unit = doLog(Level.INFO, msg)

  def logWarn(msg: String): Unit = doLog(Level.WARNING, msg)

  def logTrace(msg: String): Unit = doLog(Level.FINEST, msg)

  def logError(msg: String): Unit = doLog(Level.SEVERE, msg)

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
            doLog(Level.SEVERE, msg)
          }
        }
    }
  }

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

  // Your existing timeR logic using the new logTrace
  def timeR[A](a: => A, msg: String): A = {
    val now = System.nanoTime
    val result = a
    val millis = (System.nanoTime - now) / (1000 * 1000)
    logTrace(s"$msg (duration: $millis ms)")
    result
  }
}