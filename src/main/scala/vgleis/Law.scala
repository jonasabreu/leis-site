package vgleis

import org.eclipse.jgit.diff.DiffEntry
import java.io.PrintWriter
import java.io.File
import java.util.Scanner
import scala.collection.mutable.ListBuffer
import scala.util.Try

object Law {
  val titleTag = "(?i)<title[^>]*>([^<]*)</title>".r
  val tags = "<[^>]+>".r
  val lineStart = "(\n|^)".r

}

class Law(output : File, repoPath : String, fileName : String) {

  import Law._

  private val diffs = ListBuffer[() => String]()

  def serialize = {
    println(s"serializando $fileName")
    val writer = new PrintWriter(new File(output, fileName.replaceAll("""\.htm""", "") + ".markdown"))
    writer.println("---")
    writer.println("layout: lei")
    writer.println(s"originalUrl: ${urlFor(fileName)}")
    writer.println("tipo: federal")
    writer.println(s"title: ${title(fileName)}")
    writer.println(s"feed: /federal/$fileName.xml")
    writer.println("---\n")
    writer.println(diffs.map(_()).mkString("\n\n"))
    writer.close
  }

  def addDiff(commit : String, date : String, data : () => String) = {
    import scalatags.all._

    diffs += (() => div(cls := "law")(
      a(href := githubUrl(commit))(date),
      pre()(lineStart.replaceAllIn(tags.replaceAllIn(data(), ""), "\n\t\t"))).toString)
  }

  private def githubUrl(commit : String) =
    s"https://github.com/jonasabreu/leis-federais/blob/$commit/$fileName"

  private def urlFor(string : String) = {
    string
  }

  private def title(fileName : String) = {
    Try({
      val content = new Scanner(new File(s"$repoPath/$fileName")).useDelimiter("$$").next()
      titleTag.findAllMatchIn(content).map(_.group(1)).next
    }).getOrElse("")
  }

}