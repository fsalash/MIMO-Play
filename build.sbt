name := """play-mimo-recetas"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,PlayEbean)

scalaVersion := "2.12.6"


crossScalaVersions := Seq("2.12.6", "2.11.12")

libraryDependencies += "com.h2database" % "h2" %  "1.4.194"
libraryDependencies += guice
libraryDependencies += evolutions
libraryDependencies += jdbc
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

libraryDependencies ++= Seq(
  ehcache
)


