name := "OCScala_MagicOfSequence"

version := ".1-SNAPSHOT"

scalaVersion := "2.11.4"

resolvers ++= Seq("Sonatype Nexus releases" at "https://oss.sonatype.org/content/repositories/releases", "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq("org.scalaz" % "scalaz-concurrent_2.11" % "7.1.7",
                          "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
                         "io.argonaut" %% "argonaut" % "6.1" ) 

initialCommands in console := "import scalaz._;import Scalaz._;import scala.concurrent.Future; import scala.reflect.runtime.universe.reify; import scala.concurrent.ExecutionContext.Implicits.global;"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:higherKinds")
