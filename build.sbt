lazy val Versions = new {
  val App = "0.1"
  val Scala = "2.11.7"
  val ScalaTest = "2.2.6"
  val Htsjdk = "2.1.0"
  val LogBack = "1.1.6"
}

lazy val testDeps = Seq(
  "org.scalatest" %% "scalatest" % Versions.ScalaTest % Test
)

lazy val mainDeps = Seq(
  "org.scala-lang" % "scala-library" % Versions.Scala,
  "org.scala-lang" % "scala-compiler" % Versions.Scala,
  "org.scala-lang" % "scala-reflect" % Versions.Scala,
  "com.github.samtools" % "htsjdk" % Versions.Htsjdk,
  "ch.qos.logback" % "logback-classic" % Versions.LogBack
)

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq("-feature"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= (testDeps ++ mainDeps)
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "LoamStream Type-Safe Strawman"
  )
