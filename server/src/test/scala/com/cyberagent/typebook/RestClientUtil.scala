package com.cyberagent.typebook

import com.palantir.docker.compose.DockerComposition
import com.twitter.conversions.time._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}

import scala.language.postfixOps

trait RestClientUtil { self: SchemaRegistryBackend =>
  def withRestClient(dc: DockerComposition, label: String, tries: Int = 15)(proc: Service[Request, Response] => Any): Any = {
    val client = WaitUntilAvailable(tries) andThen Http.client.newService(getServerEndpoint(dc), label)
    proc(client)
    client.close(30 seconds)
  }
}
