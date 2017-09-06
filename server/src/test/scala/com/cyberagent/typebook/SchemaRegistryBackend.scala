package com.cyberagent.typebook

import com.palantir.docker.compose.DockerComposition
import com.palantir.docker.compose.configuration.{DockerComposeFiles, ProjectName}
import com.palantir.docker.compose.connection.waiting.HealthChecks
import com.palantir.docker.compose.connection.{DockerMachine, DockerPort}
import com.palantir.docker.compose.execution.{DefaultDockerCompose, DockerCompose}
import org.scalatest.concurrent.Eventually

import com.cyberagent.typebook.config.HttpServerConfig

import java.util.function.Function
import scala.language.postfixOps


trait SchemaRegistryBackend extends StorageBackend with RuleFixture with Eventually with Logging {

  final val Project = "test"
  final val conf = HttpServerConfig()

  // Call docker-compose and wait by waitSec after complete building
  override def dockerComposition(logDir: String): DockerComposition = DockerComposition
    .of("docker/docker-compose.yml")
    .projectName(ProjectName.fromString(Project))
    .waitingForService("backend-db", HealthChecks.toHaveAllPortsOpen)
    .waitingForService("schema-registry-server", HealthChecks.toRespondOverHttp(conf.listenPort, new Function[DockerPort, String] {
      def apply(port: DockerPort): String = port.inFormat("http://$HOST:$EXTERNAL_PORT/health")
    }))
    .saveLogsTo(s"target/dockerLogs/$logDir")
    .build()

  private def defaultDockerCompose(): DockerCompose = new DefaultDockerCompose(
    DockerComposeFiles.from("docker/docker-compose.yml"),
    DockerMachine.localMachine().build(),
    ProjectName.fromString(Project))
  def dockerComposeDown(): Unit = defaultDockerCompose().down()
  def dockerComposeKill(): Unit = defaultDockerCompose().kill()


  def getServerEndpoint(dc: DockerComposition): String = {
    val dockerPort = dc.portOnContainerWithInternalMapping("schema-registry-server", conf.listenPort)
    s"${dockerPort.getIp}:${dockerPort.getExternalPort}"
  }

}
