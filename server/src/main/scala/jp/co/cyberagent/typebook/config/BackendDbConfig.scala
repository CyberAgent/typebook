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

package jp.co.cyberagent.typebook.config

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._

case class BackendDbConfig(
  servers: String,
  user: String,
  password: Option[String],
  database: String
) {
  require(servers != null && servers.nonEmpty, "mysql servers must not be empty")
  require(user != null && user.nonEmpty, "mysql user must not be empty")
  require(database != null && database.nonEmpty, "mysql database must not be empty")
}

object BackendDbConfig {

  /**
    * Create a configuration for backend database
    * @return
    */
  def apply(): BackendDbConfig = {
    val conf = ConfigFactory.load()
    val servers = conf.as[String]("typebook.db.servers")
    apply(servers)
  }

  /**
    * Create a configuration for backend database
    * @param servers
    * @return
    */
  def apply(servers: String): BackendDbConfig = {
    val conf = ConfigFactory.load()
    val user = conf.getString("typebook.db.user")
    val password = conf.getAs[String]("typebook.db.password")
    val database = conf.getString("typebook.db.database")
    new BackendDbConfig(servers, user, password, database)
  }

}
