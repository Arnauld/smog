import sbt._

class SmogProject(info: ProjectInfo) extends DefaultWebProject(info) {

  // scalatra
  val scalatraVersion = "2.0.0-SNAPSHOT"
  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalate = "org.scalatra" %% "scalatra-scalate" % scalatraVersion
  val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"

  // Alternatively, you could use scalatra-specs
  val scalatest = "org.scalatra" %% "scalatra-specs" % scalatraVersion % "test"

  // spring-social
  val springSocialVersion = "1.0.0.M3"
  val springSocial = "org.springframework.social" % "spring-social-core" % springSocialVersion
  val springSocialTwitter = "org.springframework.social" % "spring-social-twitter" % springSocialVersion

  // scribe - oAuth'n co
  val scribe = "org.scribe" % "scribe" % "1.2.0"

  // script
  val rhino = "rhino" % "js" % "1.7R2"

  // logs
  val slf4j        = "org.slf4j" % "slf4j-api" % "1.6.0"
  val slf4jBinding = "ch.qos.logback" % "logback-classic" % "0.9.25" % "runtime"

  // utilities
  val specs   = "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.8.5" % "test"
  val junit = "junit" % "junit" % "4.8.1" % "test"

  /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * dependencies
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
  val jbossRepository = "repository.jboss.org" at "http://repository.jboss.org/nexus/content/groups/public/"

  val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  // For Scalate
  val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"
}
