package vgleis

import java.io.{ ByteArrayOutputStream, File, FileOutputStream, PrintWriter }
import java.text.SimpleDateFormat
import scala.collection.JavaConverters.{ asScalaBufferConverter, iterableAsScalaIterableConverter }
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{ DiffEntry, DiffFormatter }
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.FileUtils
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import scala.io.Source
import java.util.Scanner
import scala.collection.mutable.Map

object Generator extends App {

  val sdf = new SimpleDateFormat("yyyy-MM-dd")

  val output = new File("site/federal")
  if (output.getParentFile().exists()) {
    FileUtils.delete(output.getParentFile, FileUtils.RECURSIVE)
  }
  prepareOutput(output)

  val basePath = "/home/jonas/Documents/workspace-html/leis-federais"
  val pathToRepo = s"${basePath}/.git"

  val repo = new FileRepositoryBuilder().setGitDir(new File(pathToRepo)).build
  val git = new Git(repo)

  val commits = git.log().call().asScala.toList.take(2)

  val reader = repo.newObjectReader()

  commits.zip(commits.tail).map { e =>

    print(s"Processando commit ${e._1.getFullMessage()}")

    val diffs = generateDiffs(reader, e);

    (sdf.format(e._1.getAuthorIdent().getWhen()), diffs, e._1.getId().getName())
  }.foldLeft(Map[String, Law]()) {
    case (map, (data, diffs, commit)) =>
      println(s"gerando $data")

      diffs.foreach { e =>

        val fileName = e.getNewPath()

        if (!map.contains(fileName)) {
          map += fileName -> new Law(output, basePath, fileName)
        }

        val baos = new ByteArrayOutputStream()
        val formatter = new DiffFormatter(baos)
        formatter.setRepository(repo)
        val formattedOutput = formatter.format(e)

        map(fileName).
          addDiff(commit, data, new String(baos.toByteArray(), "ISO-8859-1"))
      }
      map
  }.foreach(_._2.serialize)

  def generateDiffs(reader : ObjectReader, e : (RevCommit, RevCommit)) = {
    val nt = new CanonicalTreeParser()
    nt.reset(reader, e._1.getTree())
    val ot = new CanonicalTreeParser()
    ot.reset(reader, e._2.getTree())

    git.diff().setShowNameAndStatusOnly(true).setNewTree(nt).setOldTree(ot).setContextLines(5).call().asScala
  }

  def prepareOutput(output : File) = {
    output.mkdirs
    val listagem = new File(output, "index.html")
    val writer = new PrintWriter(listagem)
    writer.println("---")
    writer.println("layout: lista-leis")
    writer.println("tipoLei: federal")
    writer.println("feed: /federal/federal.xml")
    writer.println("---")
    writer.close
  }

}