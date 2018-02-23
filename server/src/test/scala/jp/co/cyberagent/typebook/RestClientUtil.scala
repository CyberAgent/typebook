package jp.co.cyberagent.typebook

import scala.language.postfixOps

import com.palantir.docker.compose.DockerComposeRule
import com.twitter.conversions.time._
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}

trait RestClientUtil { self: SchemaRegistryBackend =>
  def withRestClient(dc: DockerComposeRule, label: String, tries: Int = 15)(proc: Service[Request, Response] => Any): Any = {
    val client = WaitUntilAvailable(tries) andThen Http.client.newService(getServerEndpoint(dc), label)
    proc(client)
    client.close(30 seconds)
  }
}
