/*
 * Copyright (c) 2017 CyberAgent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cyberagent.typebook

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.server.TwitterServer
import com.twitter.util.Await

import com.cyberagent.typebook.api.RegistryService
import com.cyberagent.typebook.config.HttpServerConfig
import com.cyberagent.typebook.db.{DefaultMySQLBackend, TableDefinition}


object ServerApp extends TwitterServer with DefaultMySQLBackend {

  val conf = HttpServerConfig()

  lazy val httpServer: ListeningServer = Http.server
    .configured(Http.Netty3Impl)
    .withStatsReceiver(statsReceiver)
    .serve(s":${conf.listenPort}", RegistryService() )

  private def shutdown(): Unit = {
    log.info("TypeBook is shutting down...")
    Await.ready(httpServer.close())
  }

  def main(): Unit = {
    log.info("Initializing Database...")
    TableDefinition.initializeTables()

    log.info("TypeBook is starting up...")
    onExit(shutdown())
    Await.ready(httpServer)
  }

}
