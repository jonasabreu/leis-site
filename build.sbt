
organization := "net.vidageek"

name         := "leis-site"

version in Global := "1.0"

scalaVersion := "2.10.3"


libraryDependencies ++= Seq("junit" % "junit" % "4.10", 
							"org.specs2" % "specs2_2.10" % "2.2",
							"log4j" % "log4j" % "1.2.16",
							"org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.1.201403241930-r"
							)

EclipseKeys.withSource := true

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

