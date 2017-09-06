package com.cyberagent.typebook

import com.palantir.docker.compose.DockerComposition
import com.palantir.docker.compose.connection.waiting.HealthChecks
import org.scalatest.concurrent.Eventually

import scala.language.postfixOps

trait StorageBackend extends RuleFixture with Eventually with Logging {

  def dockerComposition(logDir: String): DockerComposition = DockerComposition
    .of("docker/docker-compose.db.yml")
    .waitingForService("backend-db", HealthChecks.toHaveAllPortsOpen)
    .saveLogsTo(s"target/dockerLogs/$logDir")
    .build()

  def getStorageHostPort(dc: DockerComposition): String = {
    val port = dc.portOnContainerWithInternalMapping("backend-db", 3306)
    s"${port.getIp}:${port.getExternalPort}"
  }

}
