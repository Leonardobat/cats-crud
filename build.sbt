lazy val rootProject = (project in file("."))
  .enablePlugins(DockerPlugin)
  .enablePlugins(GitVersioning)
  .settings(
    name := "cats-crud",
    organization := "io.github.leonardobat",
    scalaVersion := "3.2.2",
    git.useGitDescribe := true,

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.5.0",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "io.circe" %% "circe-config" % "0.10.0",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "org.http4s" %% "http4s-ember-server" % "0.23.19",
      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.16.0" % Test,
      "org.scalatestplus" %% "mockito-4-11" % "3.2.16.0" % Test,
      "org.http4s" %% "http4s-circe" % "0.23.19" % Test,
    ),

    Compile / scalacOptions += "-source:3.0-migration",
    Compile / scalacOptions += "-rewrite",

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
        MergeStrategy.singleOrError
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },

    docker / dockerfile := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("ibm-semeru-runtimes:open-11-jre")
        copy(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
      }
    },

    docker / imageNames := Seq(
      // Sets the latest tag
      ImageName(s"${organization.value}/${name.value}:latest"),

      // Sets a name with a tag that contains the project version
      ImageName(
        namespace = Some(organization.value),
        repository = name.value,
        tag = Some("v" + version.value)
      )
    )
  )
