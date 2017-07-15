name := """hnotes"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val ngVersion="2.2.0"

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
  "com.github.t3hnar" %% "scala-bcrypt" % "2.6",
  //angular2 dependencies
	"org.webjars.npm" % "angular__common" % ngVersion,
	"org.webjars.npm" % "angular__compiler" % ngVersion,
	"org.webjars.npm" % "angular__core" % ngVersion,
	"org.webjars.npm" % "angular__http" % ngVersion,
	"org.webjars.npm" % "angular__forms" % ngVersion,
	"org.webjars.npm" % "angular__router" % "3.2.0",
	"org.webjars.npm" % "angular__platform-browser-dynamic" % ngVersion,
	"org.webjars.npm" % "angular__platform-browser" % ngVersion,
	"org.webjars.npm" % "systemjs" % "0.19.40",
	"org.webjars.npm" % "rxjs" % "5.0.0-beta.12",
	"org.webjars.npm" % "reflect-metadata" % "0.1.8",
	"org.webjars.npm" % "zone.js" % "0.6.26",
	"org.webjars.npm" % "core-js" % "2.4.1",
	"org.webjars.npm" % "symbol-observable" % "1.0.1",
	
	"org.webjars.npm" % "typescript" % "2.2.1",
	
	"org.webjars" % "ckeditor" % "4.7.0",
	"org.webjars.npm" % "ng2-ckeditor" % "1.1.9",
	
	//tslint dependency
	"org.webjars.npm" % "tslint-eslint-rules" % "3.4.0",
	"org.webjars.npm" % "tslint-microsoft-contrib" % "4.0.0",
	//   "org.webjars.npm" % "codelyzer" % "2.0.0-beta.1",
	"org.webjars.npm" % "types__jasmine" % "2.2.26-alpha" % "test"
	//test
	//  "org.webjars.npm" % "jasmine-core" % "2.4.1"
)


sources in EditSource <++= baseDirectory.map(d => (d / "conf" ** "config.js").get)
targetDirectory in EditSource <<= baseDirectory(_ / "public" / "client" / "js")
flatten in EditSource := true


(compile in Compile) <<= (compile in Compile) dependsOn (edit in EditSource)


dependencyOverrides += "org.webjars.npm" % "minimatch" % "3.0.0"

// use the webjars npm directory (target/web/node_modules ) for resolution of module imports of angular2/core etc
resolveFromWebjarsNodeModulesDir := true

// use the combined tslint and eslint rules plus ng2 lint rules
(rulesDirectories in tslint) := Some(List(
  tslintEslintRulesDir.value,
  ng2LintRulesDir.value
))

logLevel in tslint := Level.Debug
routesGenerator := InjectedRoutesGenerator



fork in run := true



fork in run := true