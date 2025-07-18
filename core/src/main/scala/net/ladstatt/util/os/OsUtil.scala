package net.ladstatt.util.os

/**
 * Determine which OS we are running on
 */
object OsUtil {

  sealed trait Os extends Serializable

  case object Windows extends Os

  case object Mac extends Os

  case object Linux extends Os

  /** running on linux, but virtualized on flatpak */
  case object LinuxFlatPak extends Os

  val runningInFlatPak : Boolean = Option(System.getenv("FLATPAK_ID")).isDefined

  val currentOs: Os =
    if (System.getProperty("os.name").toLowerCase.contains("windows")) {
      Windows
    } else if (System.getProperty("os.name").toLowerCase.contains("mac")) {
      Mac
    } else if (System.getProperty("os.name").toLowerCase.contains("linux")) {
      if (runningInFlatPak) {
        LinuxFlatPak
      } else {
        Linux
      }
    } else {
      Windows
    }

  val isMac: Boolean = currentOs == Mac
  val isWin: Boolean = currentOs == Windows
  val isLinux: Boolean = currentOs == Linux

  // for releases / mac installers this value should always be true
  // set this flag only during development to false
  val enableSecurityBookmarks: Boolean = isMac

  def osFun[T](onWin: => T, onMac: => T, onLinux: => T): T =
    if (isWin) {
      onWin
    } else if (isMac) {
      onMac
    } else onLinux
}
