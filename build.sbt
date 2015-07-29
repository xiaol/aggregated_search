organization  := "com.search"

name := "aggregated_search"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
                  "Spray Repository"    at "http://repo.spray.io")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    // spray
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-httpx"   % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.2",

    // akka
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,

    // test
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",

    // log
    "com.typesafe.akka"   %%  "akka-slf4j"      % akkaV,
    "ch.qos.logback"      %  "logback-classic"  % "1.1.2",

    // json
    "org.json4s"          %% "json4s-native"  % "3.2.11",

    // Html parser
    "junit"               % "junit"           % "4.12",
    "org.jsoup"           % "jsoup"           % "1.8.2",

    // redis-driver
    "net.debasishg"       %% "redisclient"    % "3.0",

    // mongo-driver
    "org.reactivemongo"   %% "reactivemongo"  % "0.11.4"
  )
}

Revolver.settings.settings
