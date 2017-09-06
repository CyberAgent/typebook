
scalaVersion in ThisBuild := "2.12.3"
crossScalaVersions := Seq("2.12.3", "2.11.11")
publishArtifact := false

// license settings
organizationName in ThisBuild := "CyberAgent"
startYear in ThisBuild := Some(2017)
licenses in ThisBuild += ("MIT", new URL("https://opensource.org/licenses/MIT"))

// The information about docker repository
val dockerImageVersion = taskKey[String]("the version for docker image of schema registry server")
val dockerRepository = "registry.hub.docker.com"

// The versions for dependency libraries
val avroVersion = "1.8.2"
val circeVersion = "0.8.0"
val ficusVersion = "1.4.1"
val finagleVersion = "6.45.0"
val finchVersion = "0.15.1"
val twitterServerVersion = "1.30.0"

// The versions for test dependency libraries
val dockerComposeRuleVersion = "0.16.0"
val junitVersion = "4.10"
val scalaTestVersion = "3.0.0"
val slf4jVersion = "1.7.21"


// make test execution sequential across projects to support docker-compose-rule integration testing
concurrentRestrictions in ThisBuild := Seq(
  Tags.limit(Tags.ForkedTestGroup, 1),
  Tags.limit(Tags.Test, 1),
  Tags.limit(Tags.Untagged, 1)
)


lazy val commonSettings = Seq(
  addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full),
  assemblyJarName in assembly := s"${name.value}_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-${version.value}",
  assemblyMergeStrategy in assembly := {
    case "BUILD" => MergeStrategy.first
    case "log4j.properties" => MergeStrategy.concat
    case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
    case x => (assemblyMergeStrategy in assembly).value(x)
  },
  organization := "com.cyberagent.typebook",
  parallelExecution in Test := false,
  publishArtifact := false,
  publishArtifact in Test := false,
  publishMavenStyle := true,
  resolvers ++= Seq(
    "Palantir Repository" at "https://dl.bintray.com/palantir/releases",
    "twttr" at "http://maven.twttr.com",
    Resolver.sonatypeRepo("releases")
  ),
  run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  test in assembly := {},
  updateOptions := updateOptions.value.withCachedResolution(true)
)


lazy val core = (project in file("core")).
  settings(commonSettings).
  settings(
    name := "core",
    libraryDependencies ++= Seq(
      "com.iheart" %% "ficus" % ficusVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.apache.avro" % "avro" % avroVersion,
      "com.palantir.docker.compose" % "docker-compose-rule" % dockerComposeRuleVersion % Test,
      "junit" % "junit" % junitVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.slf4j" % "slf4j-api" % slf4jVersion % Test,
      "org.slf4j" % "slf4j-log4j12" % slf4jVersion % Test
    ),
    publishArtifact := false
  )

lazy val server = (project in file("server")).
  enablePlugins(DockerPlugin).
  settings(commonSettings).
  settings(
    name := "typebook",
    mainClass in assembly := Some("com.cyberagent.typebook.ServerApp"),
    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finch-core" % finchVersion,
      "com.github.finagle" %% "finch-circe" % finchVersion,
      "com.twitter" %% "finagle-core" % finagleVersion,
      "com.twitter" %% "finagle-mysql" % finagleVersion,
      "com.twitter" %% "finagle-stats" % finagleVersion,
      "com.twitter" %% "twitter-server" % twitterServerVersion,
      "com.github.finagle" %% "finch-test" % finchVersion % Test,
      "com.palantir.docker.compose" % "docker-compose-rule" % dockerComposeRuleVersion % Test,
      "junit" % "junit" % junitVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  ).
  settings(
    docker := (docker dependsOn assembly).value,
    dockerfile in docker := {
      val appDir = "/opt/typebook"
      val artifact: File = assembly.value
      val artifactTargetPath = s"$appDir/${artifact.getName}"
      new Dockerfile {
        from("anapsix/alpine-java:8")
        copy(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
      }
    },
    dockerImageVersion := sys.env.getOrElse("TAG", version.value),
    imageNames in docker := Seq(
      ImageName(s"$dockerRepository/cyberagent/typebook:${dockerImageVersion.value}"),
      ImageName(s"$dockerRepository/cyberagent/typebook:latest")
    )
  ).
  dependsOn(core % "test->test;compile->compile")
