ThisBuild / scalaVersion     := "2.13.5"
ThisBuild / organization     := "com.github.jbwheatley"

Compile / scalafmtConfig := file(".scalafmt.conf")
scalafmtOnCompile := true

lazy val root = (project in file("."))
  .settings(
    name := "lgtv",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % "3.0.2"
    )
  ).settings(
    assembly /assemblyMergeStrategy := {
        case PathList("META-INF", _*) => MergeStrategy.discard
        case x if x.endsWith("module-info.class") => MergeStrategy.discard
        case v => (assembly /assemblyMergeStrategy).value(v)
    }
  ).settings(
    assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)
  )

assembly / assemblyJarName := "lgtv.jar"
