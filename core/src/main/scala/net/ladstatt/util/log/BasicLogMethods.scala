package net.ladstatt.util.log

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

trait BasicLogMethods {

  def logInfo(msg: String): Unit

  def logWarn(msg: String): Unit

  def logTrace(msg: String): Unit

  def logException(msg: String, t: Throwable): Unit

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
               , log: String => Unit = logTrace): A

}
