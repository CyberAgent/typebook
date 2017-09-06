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

package jp.co.cyberagent.typebook.db

import java.util.Date

import scala.language.postfixOps

import com.twitter.conversions.time._
import com.twitter.finagle.{Mysql => TMysql}
import com.twitter.finagle.client.DefaultPool
import com.twitter.finagle.mysql.{Client, Cursors, Transactions}
import io.circe._
import io.circe.syntax._

import jp.co.cyberagent.typebook.config.BackendDbConfig


object Mysql {

  def default(): Client with Transactions with Cursors = {
    val conf = BackendDbConfig()
    standard(conf)
  }

  def standard(conf: BackendDbConfig): Client with Transactions with Cursors = TMysql.client
    .withCredentials(conf.user, conf.password.orNull)
    .withDatabase(conf.database)
    .configured(DefaultPool.Param(
      low = 0,
      high = 100,
      idleTime = 5.minutes,
      bufferSize = 0,
      maxWaiters = Int.MaxValue
    ))
    .newRichClient(conf.servers)
}

trait MySQLBackend {
  implicit val mySqlClient: Client

  // some general mysql data type encoder/decoders
  implicit val dateEncoder: Encoder[Date] = Encoder.instance(a => a.getTime.asJson)
  implicit val dateDecoder: Decoder[Date] = Decoder.instance(a => a.as[Long].right.map(new Date(_)))
}

trait DefaultMySQLBackend extends MySQLBackend {
  override implicit val mySqlClient: Client  = Mysql.default()
}
