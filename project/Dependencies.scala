import sbt._

object Dependencies {
  lazy val scalaTest =  Seq("org.scalatest" %% "scalatest" % "3.0.8" % Test)

  lazy val fs2 = Seq(
    // available for 2.12, 2.13
    "co.fs2" %% "fs2-core" % "2.2.1", // For cats 2 and cats-effect 2
    // optional I/O library
    "co.fs2" %% "fs2-io" % "2.2.1"
  )
}
