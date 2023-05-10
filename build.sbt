val scala3Version = "3.2.2"
val AkkaVersion = "2.8.1"
val AlpakkaVersion = "6.0.0"
val AlpakkaKafkaVersion = "4.0.2"
val AkkaHttpVersion = "10.5.1"
val MariaDbDriverVersion = "3.1.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "test-compile",
    version := "0.1.0-SNAPSHOT",
    maintainer := "Ryan Deschamps <rdeschamps@conestogac.on.ca",
    organization := "com.greebiestudios",
    description := "A project for creating test server data",
    

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion, 
    libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-s3" % AlpakkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-xml" % AkkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    //libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-spring-web" % "1.0-M2",
    libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    libraryDependencies += "org.mariadb.jdbc" % "mariadb-java-client" % "3.1.4",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10",
    libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1",
    
  )

  enablePlugins(JavaServerAppPackaging)
