ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.jbwheatley"

scalafmtConfig in Compile := file(".scalafmt.conf")
scalafmtOnCompile := true

lazy val root = (project in file("."))
  .settings(
    name := "lgtv",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % "2.4.0"
    )
  ).settings(
    assemblyMergeStrategy in assembly := {
        case PathList("META-INF", _*) => MergeStrategy.discard
        case x if x.endsWith("module-info.class") => MergeStrategy.discard
        case v => (assemblyMergeStrategy in assembly).value(v)
    }
  ).settings(
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

assemblyJarName in assembly := "lgtv.jar"
