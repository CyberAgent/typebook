
scalaVersion in ThisBuild := "2.12.5"
crossScalaVersions := Seq("2.12.5", "2.11.12")
publishArtifact := false

// license settings
organizationName in ThisBuild := "CyberAgent"
startYear in ThisBuild := Some(2017)
licenses in ThisBuild += ("MIT", new URL("https://opensource.org/licenses/MIT"))

// The information about docker repository
val dockerImageVersion = taskKey[String]("the version for docker image of schema registry server")

// The versions for dependency libraries
val avroVersion = "1.8.2"
val circeVersion = "0.9.3"
val ficusVersion = "1.4.3"
val twitterVersion = "18.3.0"
val finchVersion = "0.18.1"

// The versions for test dependency libraries
val dockerComposeRuleVersion = "0.33.0"
val junitVersion = "4.12"
val scalaTestVersion = "3.0.5"
val slf4jVersion = "1.7.25"


// make test execution sequential across projects to support docker-compose-rule integration testing
concurrentRestrictions in ThisBuild := Seq(
  Tags.limit(Tags.ForkedTestGroup, 1),
  Tags.limit(Tags.Test, 1),
  Tags.limit(Tags.Untagged, 1)
)

enablePlugins(GitVersioning)

lazy val commonSettings = Seq(
  addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full),
  assemblyJarName in assembly := s"${name.value}_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-${version.value}",
  assemblyMergeStrategy in assembly := {
    case "BUILD" => MergeStrategy.first
    case "log4j.properties" => MergeStrategy.concat
    case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
    case x => (assemblyMergeStrategy in assembly).value(x)
  },
  organization := "jp.co.cyberagent.typebook",
  parallelExecution in Test := false,
  publishArtifact := false,
  publishArtifact in Test := false,
  publishMavenStyle := true,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("palantir", "releases")
  ),
  run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  javaOptions ++= Seq("-Dfile.encoding=UTF8"),
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
    mainClass in assembly := Some("jp.co.cyberagent.typebook.ServerApp"),
    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finch-core" % finchVersion,
      "com.github.finagle" %% "finch-circe" % finchVersion,
      "com.twitter" %% "finagle-core" % twitterVersion,
      "com.twitter" %% "finagle-mysql" % twitterVersion,
      "com.twitter" %% "finagle-stats" % twitterVersion,
      "com.twitter" %% "twitter-server" % twitterVersion,
      "com.github.finagle" %% "finch-test" % finchVersion % Test,
      "com.palantir.docker.compose" % "docker-compose-rule-junit4" % dockerComposeRuleVersion % Test,
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
      ImageName(s"cyberagent/typebook:${dockerImageVersion.value}"),
      ImageName(s"cyberagent/typebook:latest")
    )
  ).
  dependsOn(core % "test->test;compile->compile")
