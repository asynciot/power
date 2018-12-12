name := """power"""

version := "1.0-SNAPSHOT"


lazy val root = (project in file(".")).enablePlugins(PlayJava, LauncherJarPlugin)
  .dependsOn(common,account,device,other)

lazy val common = (project in file("modules/common"))
  .enablePlugins(PlayJava, PlayEbean)

lazy val account = (project in file("modules/account"))
  .enablePlugins(PlayJava,PlayEbean)
  .dependsOn(common)

lazy val device = (project in file("modules/device"))
  .enablePlugins(PlayJava,PlayEbean)
  .dependsOn(common,account)

lazy val other = (project in file("modules/other"))
  .enablePlugins(PlayJava,PlayEbean)
  .dependsOn(common)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  "mysql" % "mysql-connector-java" % "8.0.13",
  javaWs
)
