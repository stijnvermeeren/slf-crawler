name := "slfcrawler"

version := "1.0"

scalaVersion := "2.11.7"

resolvers := Seq(
  "mvnrepository" at "http://mvnrepository.com/artifact/"
)

libraryDependencies += "com.github.seratch" %% "awscala" % "0.5.+"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies += "org.apache.commons" % "commons-text" % "1.1"
