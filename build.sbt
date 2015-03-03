name := "pact-jvm-provider-spring-mvc"

version := "0.3.0"

organization := "com.reagroup"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

sbtVersion := "0.13.7"

val sonatypeSnapshots = "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

val sonatypeReleases = "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases"

resolvers ++= Seq(sonatypeSnapshots, sonatypeReleases)

libraryDependencies ++= Seq(
  "au.com.dius" %% "pact-jvm-model" % "2.1.11",
  "au.com.dius" %% "pact-jvm-consumer-junit" % "2.1.10",
  "org.springframework" % "spring-test" % "4.1.3.RELEASE",
  "org.springframework" % "spring-webmvc" % "4.1.3.RELEASE",
  "org.springframework" % "spring-context" % "4.1.3.RELEASE",
  "org.springframework" % "spring-core" % "4.1.3.RELEASE",
  "junit" % "junit" % "4.12",
  "org.skyscreamer" % "jsonassert" % "1.2.3",
  "javax.servlet" % "javax.servlet-api" % "3.0.1",
  "org.mockito" % "mockito-core" % "1.9.5",
  "com.novocode" % "junit-interface" % "0.11" % "test", // in order to run Junit tests in SBT​
  "org.specs2" %% "specs2" % "2.4.2" % "test"
)

publishMavenStyle := true

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo := {
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some(sonatypeSnapshots)
  else
    Some(sonatypeReleases)
}
