name := "pact-jvm-provider-spring-mvc"

version := "0.5.0"

organization := "com.reagroup"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.4", "2.11.4")

sbtVersion := "0.13.11"

libraryDependencies ++= Seq(
  "au.com.dius" %% "pact-jvm-model" % "3.2.12",
  "au.com.dius" %% "pact-jvm-consumer-junit" % "3.2.12",
  "org.springframework" % "spring-test" % "4.3.2.RELEASE",
  "org.springframework" % "spring-webmvc" % "4.3.2.RELEASE",
  "org.springframework" % "spring-context" % "4.3.2.RELEASE",
  "org.springframework" % "spring-core" % "4.3.2.RELEASE",
  "junit" % "junit" % "4.12",
  "org.skyscreamer" % "jsonassert" % "1.2.3",
  "javax.servlet" % "javax.servlet-api" % "3.0.1",
  "org.mockito" % "mockito-core" % "1.9.5",
  "com.novocode" % "junit-interface" % "0.11" % "test", // in order to run Junit tests in SBT​
  "org.specs2" %% "specs2" % "2.4.2" % "test"
)

// publish to sonatype
import xerial.sbt.Sonatype.SonatypeKeys._

sonatypeSettings

profileName := "com.reagroup"

pomExtra := {
  <url>https://github.com/realestate-com-au/pact-jvm-provider-spring-mvc</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git://github.com/realestate-com-au/pact-jvm-provider-spring-mvc.git</connection>
    <developerConnection>scm:git:git@github.com:realestate-com-au/pact-jvm-provider-spring-mvc.git</developerConnection>
    <url>https://github.com/realestate-com-au/pact-jvm-provider-spring-mvc</url>
  </scm>
  <developers>
    <developer>
      <id>freewind</id>
      <name>Peng Li</name>
      <url>http://github.com/freewind/</url>
      <email>pli@thoughtworks.com</email>
    </developer>
  </developers>
}
