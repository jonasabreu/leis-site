package vgleis

import java.io.File
import scala.collection.mutable.ListBuffer
import java.io.PrintWriter
import scala.collection.immutable.Seq

class Feeds(output : File) {

  val principal = new ListBuffer[Entry]()

  def add(tipo : String, data : String, fileName : String) {
    principal += Entry(tipo, data, fileName)
  }

  def serialize {
    val all = principal.map(e => (e.fileName, e)).groupBy(_._1).map {
      case (fileName, l) => l.maxBy(_._2.data)
    }.
      values.
      toBuffer.
      sortWith(_.data > _.data).toList

    write(all.take(200), "leis.xml", all.head.data)
    val tiposFeed = all.groupBy(_.tipo).map {
      case (tipo, entries) => write(entries.sortWith(_.data > _.data).take(100), s"$tipo/$tipo.xml", entries.head.data)
    }

    val filesFeed = all.groupBy(_.fileName).map {
      case (file, entries) => write(entries.sortWith(_.data > _.data).take(10), s"${entries.head.tipo}/$file.xml", entries.head.data)
    }
  }

  def write(entries : List[Entry], file : String, data : String) {
    val writer = new PrintWriter(new File(output, file))
    writer.println("---")
    writer.println("layout: nil")
    writer.println("---")
    writer.println("""<?xml version="1.0" encoding="utf-8"?>""")
    writer.println("""<feed xmlns="http://www.w3.org/2005/Atom">""")
    writer.println("<title>Histórico de Leis Brasileiras</title>")
    writer.println(s"""<link href="http://leis.vidageek.net/$file" rel="self"/>""")
    writer.println("""<link href="http://leis.vidageek.net/"/>""")
    writer.println(s"<updated>${data}T08:00:00Z</updated>")
    writer.println(s"<id>http://leis.vidageek.net/$file</id>")
    writer.println("<author>")
    writer.println("<name>Jonas Abreu</name>")
    writer.println("<email>jonas@vidageek.net</email>")
    writer.println("</author>")

    entries.foreach { entry =>
      writer.println("<entry>")
      writer.println(s"<title>${entry.data} - ${entry.fileName}</title>")
      writer.println(s"""<link href="http://leis.vidageek.net/${entry.tipo}/${entry.fileName}"/>""")
      writer.println(s"<updated>${entry.data}T08:00:00Z</updated>")
      writer.println(s"<id>http://leis.vidageek.net/${entry.tipo}/${entry.fileName}</id>")
      writer.println("""<content type="html">""")
      writer.println("</content>")
      writer.println("</entry>")
    }
    writer.println("</feed>")
    writer.close
  }

}

case class Entry(tipo : String, data : String, fileName : String)