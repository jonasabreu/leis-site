package vgleis

import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.api.Git
import scala.collection.JavaConverters._
import java.util.Date
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.text.SimpleDateFormat
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import org.eclipse.jgit.util.FileUtils

object Generator extends App {

  val tags = "<[^>]+>".r

  val sdf = new SimpleDateFormat("yyyy-MM-dd")

  val output = new File("site")
  FileUtils.delete(output, FileUtils.RECURSIVE)
  output.mkdir

  val pathToRepo = "/home/jonas/Documents/workspace-html/leis-federais/.git"

  val repo = new FileRepositoryBuilder().setGitDir(new File(pathToRepo)).build
  val git = new Git(repo)

  val commits = git.log().call().asScala.toList //.take(20)

  val reader = repo.newObjectReader()

  commits.zip(commits.tail).map { e =>

    println(e._1.getFullMessage())

    val nt = new CanonicalTreeParser()
    nt.reset(reader, e._1.getTree())
    val ot = new CanonicalTreeParser()
    ot.reset(reader, e._2.getTree())

    val diffs = git.diff().setNewTree(nt).setOldTree(ot).call().asScala

    (sdf.format(e._1.getAuthorIdent().getWhen()), diffs)
  }.map {
    case (data, diffs) =>
      println(s"generating $data")
      diffs.foreach { e =>
        val file = new File(output, e.getNewPath + ".markdown")
        val baos = new ByteArrayOutputStream()
        val formatter = new DiffFormatter(baos)
        formatter.setRepository(repo)
        val formattedOutput = formatter.format(e)
        val writer = new PrintWriter(file)
        writer.println(s"$data\n\n")
        writer.append(removeTags(new String(baos.toByteArray(), "ISO-8859-1")))
        writer.close()
      }
  }

  def removeTags(string : String) = {
    string
  }

}