package vgleis

import java.io.{ ByteArrayOutputStream, File, FileOutputStream, PrintWriter }
import java.text.SimpleDateFormat

import scala.collection.JavaConverters.{ asScalaBufferConverter, iterableAsScalaIterableConverter }

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{ DiffEntry, DiffFormatter }
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.FileUtils

object Generator extends App {

  val tags = "<[^>]+>".r
  val lineStart = "(\n|^)".r

  val sdf = new SimpleDateFormat("yyyy-MM-dd")

  val output = new File("site/federal")
  if (output.getParentFile().exists()) {
    FileUtils.delete(output.getParentFile, FileUtils.RECURSIVE)
  }
  output.mkdirs

  val pathToRepo = "/home/jonas/Documents/workspace-html/leis-federais/.git"

  val repo = new FileRepositoryBuilder().setGitDir(new File(pathToRepo)).build
  val git = new Git(repo)

  val commits = git.log().call().asScala.toList.take(20)

  val reader = repo.newObjectReader()

  commits.zip(commits.tail).map { e =>

    print(s"Processando commit ${e._1.getFullMessage()}")

    val nt = new CanonicalTreeParser()
    nt.reset(reader, e._1.getTree())
    val ot = new CanonicalTreeParser()
    ot.reset(reader, e._2.getTree())

    val diffs = git.diff().setNewTree(nt).setOldTree(ot).setContextLines(5).call().asScala

    (sdf.format(e._1.getAuthorIdent().getWhen()), diffs, e._1.getId().getName())
  }.map {
    case (data, diffs, commit) =>
      println(s"gerando $data")
      diffs.foreach { e =>
        val writer = writerFor(e, commit)

        val baos = new ByteArrayOutputStream()
        val formatter = new DiffFormatter(baos)
        formatter.setRepository(repo)
        val formattedOutput = formatter.format(e)
        writer.append(diffAsHtml(github(e.getNewPath(), commit), data))
        writer.append(preparaDiffs(new String(baos.toByteArray(), "ISO-8859-1")))
        writer.close()
      }
  }

  def diffAsHtml(githubUrl : String, diff : String) = {
    import scalatags.all._
    a(href := githubUrl)(diff).toString
  }

  def writerFor(e : DiffEntry, commit : String) = {
    val file = new File(output, e.getNewPath.replaceAll("""\.htm""", "") + ".markdown")
    val newFile = !file.exists()
    val writer = new PrintWriter(new FileOutputStream(file, true))
    if (newFile) {
      addFrontMatter(writer, e.getNewPath, commit)
    }
    writer
  }

  def addFrontMatter(writer : PrintWriter, fileName : String, commit : String) {
    writer.println("---")
    writer.println("layout: lei")
    writer.println(s"url: ${urlFor(fileName)}")
    writer.println("---")
  }

  def github(fileName : String, commit : String) = {
    s"https://github.com/jonasabreu/leis-federais/blob/$commit/$fileName"
  }

  def urlFor(string : String) = {
    string
  }

  def preparaDiffs(string : String) = {
    lineStart.replaceAllIn(tags.replaceAllIn(string, ""), "\n\t")
  }

}