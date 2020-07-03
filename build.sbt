// TODO support scalajs eventually

lazy val scala213 = "2.13.3"
lazy val scala212 = "2.12.11"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala213, scala212, scala211)

ThisBuild / organization := "io.github.littlenag"
ThisBuild / scalaVersion := scala212

ThisBuild / resolvers ++= Seq(
  "Sonatype OSS Snapshots" at
    "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at
    "https://oss.sonatype.org/content/repositories/releases"
)

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / licenses := List("BSD-style" -> new URL("http://opensource.org/licenses/BSD-3-Clause"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/littlenag/coroutines"),
    "scm:git:git@github.com:littlenag/coroutines.git"
  )
)
ThisBuild / homepage := Some(url("http://storm-enroute.com/"))
ThisBuild / developers := List(
  Developer(
    id    = "littlenag",
    name  = "Mark Kegel",
    email = "mark.kegel@gmail.com",
    url   = url("http://littlenag.github.io")
  ),
  Developer(
    id    = "axel22",
    name  = "Aleksandar Prokopec",
    email = "",
    url   = url("http://axel22.github.com/")
  )
)

lazy val coroutines = (project in file("."))
  .aggregate(`coroutines-common`, `coroutines-impl`, `coroutines-extra`) //, `coroutines-benchmark`)
  .settings(
    name := "coroutines",
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val scalatestDep = "org.scalatest" %% "scalatest" % "3.2.0" % Test

lazy val `coroutines-common` = (project in file("coroutines-common"))
   .settings(
     name := "coroutines-common",
     crossScalaVersions := supportedScalaVersions,
     scalacOptions ~= filterTooStrictOptions,
     libraryDependencies ++= Seq(
       "org.scala-lang" % "scala-reflect" % scalaVersion.value,
       scalatestDep
     ),
     //ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,
     publishMavenStyle := true,
     publishArtifact in Test := false,
   )

lazy val `coroutines-impl` = (project in file("coroutines-impl"))
  .dependsOn(`coroutines-common` % "compile->compile;test->test")
  .settings(
    name := "coroutines-impl",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.storm-enroute" %% "scalameter" % "0.19" % Test,
      scalatestDep
    ),
    //testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    //ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,
    publishMavenStyle := true,
    publishArtifact in Test := false
  )

lazy val `coroutines-extra` = (project in file("coroutines-extra"))
  .dependsOn(`coroutines-impl` % "compile->compile;test->test")
  .settings(
    name := "coroutines-extra",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      scalatestDep
    ),
    //testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    //ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,
    publishMavenStyle := true,
    publishArtifact in Test := false
  )


def scalacOptionsC(scalaVersion: String) = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 11)) => Seq(
      "-deprecation",
      "-unchecked",
      "-optimise",
      "-Yinline-warnings"
    )
    case Some((2, 12)) => Seq(
      "-deprecation",
      "-unchecked"
    )
    case Some((2, 13)) => Seq(
      "-deprecation",
      "-unchecked"
    )
    case _ => throw new RuntimeException(s"Unsupported version: $scalaVersion")
  }
}

val filterTooStrictOptions = { options: Seq[String] =>
  options.filterNot(Set(
    "-Xfatal-warnings"
  ))
}

//lazy val `coroutines-benchmark` = (project in file("coroutines-benchmark"))
//  .dependsOn(`coroutines-impl`)
//  .settings(
//    publish / skip := true,
//    libraryDependencies ++= Seq("org.scala-lang.modules" % "scala-async_2.11" % "0.9.5" % "test;bench",
//     "com.storm-enroute" %% "scalameter" % "0.18"),
//  )
//  .settings(coroutinesBenchmarkSettings: _*)
