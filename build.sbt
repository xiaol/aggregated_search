organization  := "com.search"

name := "aggregated_search"

version := "1.0"

//logLevel := sbt.Level.Debug

scalaVersion := "2.11.6"
scalaVersion in ThisBuild := "2.11.6"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
                  "Spray Repository"    at "http://repo.spray.io",
                  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    // akka
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,

    // spray
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-httpx"   % sprayV,

    // json
    "org.json4s"          %% "json4s-native"  % "3.2.11",
    "io.spray"            %%  "spray-json"    % "1.3.2",

    // test
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "3.6.4" % "test",
    "org.scalatest"       %   "scalatest_2.11"% "2.2.4",
    "junit"               %   "junit"         % "4.12",

    // log
    "com.typesafe.akka"   %%  "akka-slf4j"      % akkaV,
    "ch.qos.logback"      %  "logback-classic"  % "1.1.2",

    // Html parser
    "org.jsoup"           % "jsoup"           % "1.8.3",

    // redis-driver
    "net.debasishg"       %% "redisclient"    % "3.0",

    // mongo-driver
    "org.reactivemongo"   %% "reactivemongo"  % "0.11.7",

    // base64 encode/decode
    "commons-codec"       % "commons-codec"   % "1.10",

    // NLP
    "edu.stanford.nlp"    % "stanford-corenlp" % "3.5.2",

    // selenium webdriver
    "org.seleniumhq.selenium" % "selenium-java" % "2.47.1"
  )
}

Revolver.settings.settings

assemblyMergeStrategy in assembly := {
  case PathList("org", "ansj", xs @ _*)                   => MergeStrategy.first
  case PathList("org", "apache", xs @ _*)                 => MergeStrategy.first
  case PathList("org", "nlpcn", xs @ _*)                  => MergeStrategy.first
  case PathList("org", "w3c", xs @ _*)                    => MergeStrategy.first
  case PathList("org", "xml", xs @ _*)                    => MergeStrategy.first
  case PathList("javax", "xml", xs @ _*)                  => MergeStrategy.first
  case PathList("edu", "stanford", xs @ _*)               => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".dic"       => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".data"      => MergeStrategy.first
//  case "application.conf"                             => MergeStrategy.concat
//  case "unwanted.txt"                                 => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}