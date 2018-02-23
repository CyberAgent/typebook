package jp.co.cyberagent.typebook

import scala.language.postfixOps

import com.palantir.docker.compose.{DockerComposeRule, ImmutableDockerComposeRule}
import com.palantir.docker.compose.connection.waiting.HealthChecks
import org.scalatest.concurrent.Eventually

trait StorageBackend extends RuleFixture with Eventually with Logging {

  def dockerComposition(logDir: String): ImmutableDockerComposeRule = DockerComposeRule.builder()
    .file("docker/docker-compose.db.yml")
    .waitingForService("backend-db", HealthChecks.toHaveAllPortsOpen)
    .saveLogsTo(s"target/dockerLogs/$logDir")
    .build()

  def getStorageHostPort(dc: DockerComposeRule): String = {
    val port = dc.containers().container("backend-db").port(3306)
    s"${port.getIp}:${port.getExternalPort}"
  }

}
