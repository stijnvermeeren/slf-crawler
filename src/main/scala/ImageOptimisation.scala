import java.io.File

import sys.process._

object ImageOptimisation {
  /**
    * Use system tools for image conversion, resizing and reducing colour depth.
    * Required ImageMagick and PngQuant to be installed.
    *
    * No error handling for now...
    */
  def optimise(inPath: String, outPath: String, resizeWidth: Option[Int], colorDepth: Int = 40): Unit = {
    val tmpFile = File.createTempFile("imageOptimisation_intermediate", ".png")

    val resizeArg = resizeWidth.map(width => s"-resize $width").getOrElse("")
    s"convert $inPath $resizeArg ${tmpFile.getPath}".!

    s"pngquant -f -o $outPath $colorDepth ${tmpFile.getPath}".!

    tmpFile.delete()
  }
}
