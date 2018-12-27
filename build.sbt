name := """play-mimo-recetas"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.6"

enablePlugins (PlayEbean)
crossScalaVersions := Seq("2.12.6", "2.11.12")

libraryDependencies += "com.h2database" % "h2" %  "1.4.194"
libraryDependencies += guice
libraryDependencies += evolutions
libraryDependencies += jdbc

libraryDependencies ++= Seq(
  ehcache
)