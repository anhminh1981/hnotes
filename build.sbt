name := """hnotes"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  evolutions,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.h2database" % "h2" % "1.4.187",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.pauldijou" %% "jwt-play-json" % "0.8.0",
  "org.mockito" % "mockito-core" % "2.2.9",
  "com.github.t3hnar" %% "scala-bcrypt" % "2.6"
)


sources in EditSource <++= baseDirectory.map(d => (d / "conf" ** "config.js").get)
targetDirectory in EditSource <<= baseDirectory(_ / "public" / "client" / "js")
flatten in EditSource := true


(compile in Compile) <<= (compile in Compile) dependsOn (edit in EditSource)

fork in run := true

fork in run := true

fork in run := true