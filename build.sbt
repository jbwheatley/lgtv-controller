import Dependencies._

ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.jbwheatley"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

lazy val root = (project in file("."))
  .settings(
    name := "lgtv",
    libraryDependencies ++= Seq(
        cats,
        catsEffect,
        typesafeConfig,
        ficus
    )
  )

