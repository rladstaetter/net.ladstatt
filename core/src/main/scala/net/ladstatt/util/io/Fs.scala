package net.ladstatt.util.io

import net.ladstatt.util.log.BasicLogMethods

import java.nio.charset.Charset
import java.nio.file._

/**
 * File related operations
 */
trait Fs {
  self: BasicLogMethods =>

  def createDirectories(path: Path): Unit = {
    if (Files.exists(path)) {
      logTrace(s"Using directory '${path.toAbsolutePath.toString}'")
    } else {
      Files.createDirectories(path)
      logTrace(s"Created directory '${path.toAbsolutePath.toString}'")
    }
  }

  def write(path: Path, content: String): Unit = timeR({
    createDirectories(path.getParent)
    Files.write(path, content.getBytes(Charset.forName("UTF-8")))
  }, s"Wrote '${path.toAbsolutePath.toString}'")

}
