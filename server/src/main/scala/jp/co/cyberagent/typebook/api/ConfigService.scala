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

package jp.co.cyberagent.typebook.api

import java.util.NoSuchElementException

import com.twitter.io.Buf
import com.twitter.logging.Logger
import com.twitter.util.Future
import io.circe.DecodingFailure
import io.finch._
import io.finch.circe._

import jp.co.cyberagent.typebook.db.{ConfigClient, DefaultMySQLBackend, MySQLBackend, SubjectClient}
import jp.co.cyberagent.typebook.model.{Config, ErrorResponse, RegistryConfig}


object ConfigService extends ConfigServiceTrait with DefaultMySQLBackend {
  def apply() = set :+: setProperty :+: read :+:
                readProperty :+: del :+: delProperty
}

trait ConfigServiceTrait extends ErrorHandling { self: MySQLBackend =>

  type UpdatedRows = Long
  type InsertId = Long
  type DeletedRows = Long

  private val log = Logger.get(this.getClass)

  /**
    * PUT /config/(subject: string)
    * BODY: configurations (key, value) in json format
    * Set configs under the specified subject
    */
  val set: Endpoint[UpdatedRows] = put(
    "config" :: path[String] :: jsonBody[RegistryConfig]
  ) { (subject: String, config: RegistryConfig) =>
    ConfigClient.set(subject, config).map(Ok(_).withHeader("Content-Type" -> "text/plain"))
  } handle {
    case _: DecodingFailure =>
      val msg = "Invalid config"
      log.warning(msg)
      UnprocessableEntity(ErrorResponse(422, msg))
    case _: NoSuchElementException =>
      val msg = "Invalid compatibility value"
      log.warning(msg)
      UnprocessableEntity(ErrorResponse(422, msg))
  } handle backendErrors

  /**
    * PUT /config/(subject: string)/properties/(property: string)
    * BODY value for the property
    * Set a specific property of config under the specified subject
    */
  val setProperty: Endpoint[UpdatedRows] = put(
    "config" :: path[String] :: "properties" :: path[String] :: stringBody
  ).as[Config] mapOutputAsync { config =>
    ConfigClient.set(config.subject, RegistryConfig.fromSeq(Seq(config))).map(Ok(_).withHeader("Content-Type" -> "text/plain")) // FIXME only update to weaker restriction should be allowed ???
  } handle {
    case ns: NoSuchElementException =>
      val msg = "Invalid compatibility value is provided"
      log.warning(msg)
      UnprocessableEntity(ErrorResponse(422, msg))
  } handle backendErrors


  /**
    * GET /config/(subject: string)
    * Read configurations under the specified subject in a json format
    */
  val read: Endpoint[RegistryConfig] = get(
    "config" :: path[String]
  ) { subject: String =>
    SubjectClient.read(subject).flatMap {
      case None => Future.value(NotFound(ErrorResponse(404, "Non existent subject")))
      case Some(_) => ConfigClient.read(subject).map { configs =>
        Ok(RegistryConfig.fromSeq(configs))
      }
    }

  } handle backendErrors


  /**
    * GET /config/(subject: string)/properties/(property: string)
    * Read a value for a specific property
    */
  val readProperty: Endpoint[Buf] = get(
    "config" :: path[String] ::"properties" :: path[String].should("be a valid property")(RegistryConfig.Properties.contains)
  ) { (subject: String, property: String) =>
    ConfigClient.readProperty(subject, property).map {
      case None => RegistryConfig.default.toMap.get(property) match {
        case None => NotFound(ErrorResponse(422, "Invalid Configuration Key"))
        case Some(value) => Ok(Buf.Utf8(value)).withHeader("Content-Type" -> "text/plain")
      }
      case Some(value) => Ok(Buf.Utf8(value)).withHeader("Content-Type" -> "text/plain")
    }
  } handle backendErrors


  /**
    * DELETE /config/(subject: string)
    * Delete all configs under the subject
    */
  val del: Endpoint[DeletedRows] = delete(
    "config" :: path[String]
  ) { subject: String =>
    ConfigClient.delete(subject).map(Ok(_).withHeader("Content-Type" -> "text/plain"))
  } handle backendErrors


  /**
    * DELETE /config/(subject: string)/properties/(property: string)
    * Delete a specific property under the subject
    */
  val delProperty: Endpoint[DeletedRows] = delete(
    "config" :: path[String] :: "properties" :: path[String]
  ) { (subject: String, property: String) =>
    ConfigClient.deleteProperty(subject, property).map(Ok(_).withHeader("Content-Type" -> "text/plain"))
  } handle backendErrors

}
