import sbtassembly.AssemblyPlugin.defaultShellScript

name := "libisabelle-example"
version := "0.1"

val libisabelleVersion = "0.6.6"
val scalogVersion = "0.2.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "info.hupel" %% "libisabelle" % libisabelleVersion,
  "info.hupel" %% "libisabelle-setup" % libisabelleVersion,
  "info.hupel" %% "pide-package" % libisabelleVersion,
  "io.rbricks" %% "scalog-backend" % scalogVersion,
  "io.rbricks" %% "scalog-mdc" % scalogVersion,
  "com.github.alexarchambault" %% "case-app" % "1.1.3"
)

assemblyJarName in assembly := s"example-assembly-${version.value}"
assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultShellScript))
assemblyMergeStrategy in assembly := {
  case PathList(".libisabelle", ".files") => MergeStrategy.concat
  case path => (assemblyMergeStrategy in assembly).value(path)
}

isabelleVersions := Seq("2016", "2016-1")
isabelleSessions in Compile := Seq("Example")
enablePlugins(LibisabellePlugin)

TaskKey[File]("script") := {
  val executable = assembly.value.getCanonicalPath()
  val script = (baseDirectory in ThisBuild).value / "example"
  val text = s"""
    |#!/usr/bin/env bash
    |
    |exec "$executable" "$$@"
    |""".stripMargin.trim
  streams.value.log.info(s"Writing script to $script ...")
  IO.write(script, text)
  script.setExecutable(true)
  script
}
