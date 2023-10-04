val scala3Version = "3.2.2"
val AkkaVersion = "2.8.1"
val AlpakkaVersion = "6.0.0"
val AlpakkaKafkaVersion = "4.0.2"
val AkkaHttpVersion = "10.5.1"
val MariaDbDriverVersion = "3.1.4"
val ApacheCommonsMathVersion = "3.6.1"
val ScalaLoggingVersion = "3.9.4"
val LogbackVersion = "1.2.10"
val PoiVersion = "5.2.4"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val root = project
  .in(file("."))
  .settings(
    name := "test-compile",
    version := "0.1.0-SNAPSHOT",
    maintainer := "Ryan Deschamps <rdeschamps@conestogac.on.ca",
    organization := "com.greebiestudios",
    description := "A project for creating test server data",
    dockerExposedPorts := Seq(8099),
    dockerUpdateLatest := true,
    packageName in Docker := "ryandeschamps/test-compile",
    

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.1.7",
    
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion, 
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion,
    libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-s3" % AlpakkaVersion,
    libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-csv" % AlpakkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-xml" % AkkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    //libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-spring-web" % "1.0-M2",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    libraryDependencies += "org.mariadb.jdbc" % "mariadb-java-client" % MariaDbDriverVersion,
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % LogbackVersion,
    libraryDependencies += "org.apache.commons" % "commons-math3" % ApacheCommonsMathVersion,
    libraryDependencies += "com.lihaoyi" %% "requests" % "0.8.0",
    libraryDependencies += "org.apache.poi" % "poi" % PoiVersion,
    libraryDependencies += "org.apache.poi" % "poi-ooxml" % PoiVersion,
    
  )

  enablePlugins(JavaServerAppPackaging)
  enablePlugins(DockerPlugin)

