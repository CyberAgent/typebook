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

import scala.language.postfixOps

import com.twitter.finagle.mysql._
import com.twitter.util.Future

import jp.co.cyberagent.typebook.model.{Config, RegistryConfig}


object ConfigClient extends RaisingServerError {

  type UpdatedRows = Long
  type DeletedRows = Long

  /**
    * Set a server config
    * For existing properties, updating with given values
    * For not existing properties, newly registering
    * @param subject
    * @param config
    * @param client
    * @return
    */
  def set(subject: String, config: RegistryConfig)(implicit client: Client): Future[UpdatedRows] = Future.collect(
    config.toConfigs(subject).map { config => client.prepare(
      "INSERT INTO `configs` (subject, property, value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value = ?"
    )(config.subject, config.property, config.value, config.value) map {
      case _: OK => 1L
      case _ => 0L
    }}
  ) map(_.sum)


  /**
    * Set a server config by the seq of Config
    * For existing properties, updating with given values
    * For not existing properties, newly registering
    * @param props
    * @param client
    * @return the number of inserted rows to the db
    */
//  def set(props: Seq[Config])(implicit client: Client): Future[UpdatedRows] = Future.collect(
//      props.groupBy(_.subject) map { case (subject: String, configs: Seq[Config]) =>
//        set(subject, RegistryConfig.fromSeq(configs))
//      } toSeq
//    ) map(_.sum)


  /**
    * Set a value to a property
    * If it is existing, conducting update otherwise newly registering
    * @param prop
    * @param client
    * @return
    */
  def set(prop: Config)(implicit client: Client): Future[UpdatedRows] = client.prepare(
    "INSERT INTO `configs` (subject, property, value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value = ?"
  )(prop.subject, prop.property, prop.value, prop.value) map {
    case _: OK => 1L
    case _ => 0L
  }


  /**
    * Read configs under the specified subject
    * @param subject
    * @param client
    * @return
    */
  def read(subject: String)(implicit client: Client): Future[Seq[Config]] = client.prepare(
    "SELECT * FROM `configs` WHERE subject = ?"
  )(subject) map (result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map(convertToEntity)
  })


  /**
    * Read a property under the specified subject
    * @param subject
    * @param property
    * @param client
    * @return
    */
  def readProperty(subject: String, property: String)(implicit client: Client): Future[Option[String]] = client.prepare(
    "SELECT value FROM `configs` WHERE subject = ? AND property = ?"
  )(subject, property) map (result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.headOption.map ( _("value").get.asInstanceOf[StringValue].s )
  })


  /**
    * Delete all configs under the specified subject
    * @param subject
    * @param client
    * @return
    */
  def delete(subject: String)(implicit client: Client): Future[DeletedRows] = client.prepare(
    "DELETE FROM `configs` WHERE subject = ?"
  )(subject) map ( result => withRaisingException(result) {
    result.asInstanceOf[OK].affectedRows
  })


  /**
    * Delete a property under the specified subject
    * @param subject
    * @param property
    * @param client
    * @return
    */
  def deleteProperty(subject: String, property: String)(implicit client: Client): Future[DeletedRows] = client.prepare(
    "DELETE FROM `configs` WHERE subject = ? AND property = ?"
  )(subject, property) map ( result => withRaisingException(result) {
    result.asInstanceOf[OK].affectedRows
  })


  def convertToEntity(row: Row): Config = {
    val StringValue(subject) = row("subject").get
    val StringValue(property) = row("property").get
    val StringValue(value) = row("value").get
    Config(subject,property,value)
  }
}
