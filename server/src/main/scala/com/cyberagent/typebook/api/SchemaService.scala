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

package com.cyberagent.typebook.api

import com.twitter.logging.Logger
import com.twitter.util.Future
import io.finch._
import io.finch.circe._
import org.apache.avro.{Schema => AvroSchema}

import com.cyberagent.typebook.db.{ConfigClient, DefaultMySQLBackend, MySQLBackend, SchemaClient}
import com.cyberagent.typebook.compatibility.{CompatibilityUtil, SchemaCompatibility}
import com.cyberagent.typebook.compatibility.SchemaCompatibility.SchemaCompatibility
import com.cyberagent.typebook.model._
import com.cyberagent.typebook.version.{SemanticVersion, VersioningRule}

import scala.language.postfixOps


object SchemaService extends SchemaServiceTrait with DefaultMySQLBackend {

  def apply() = create :+: lookup :+: lookupAll :+: readById :+:
                readByVersion :+: readVersions :+: checkCompatibility
}


trait SchemaServiceTrait extends ErrorHandling { self: MySQLBackend =>

  private val log = Logger.get(this.getClass)

  /**
    * parse a given string version and read the corresponding schema
    * @param subject
    * @param version
    * @return
    */
  private def parseAndReadByVersion(subject: String, version: String): Future[Option[Schema]] = version.trim match {
    case "latest" => SchemaClient.readByVersion(subject)
    case semanticVersion if "v([1-9][0-9]*)".r.pattern.matcher(semanticVersion).matches() => SchemaClient.readByVersion(subject, version.substring(1).toInt)
    case v => SchemaClient.readByVersion(subject, SemanticVersion(v))
  }

  /**
    * POST /subjects/(subject: string)/versions
    * BODY: Avro schema definition
    * Register a new schema under the given subject
    */
  val create: Endpoint[SchemaId] = post(
    "subjects" :: path[String] ::
    "versions" :: jsonBody[AvroSchema]
  ) { (subject: String, avroSchema: AvroSchema) => withLatestMajorSchemasAndRestriction(subject) { (latestMajorSchemas: Seq[Schema], restriction: SchemaCompatibility) =>
    if (!CompatibilityUtil.checkCompatibility(avroSchema, latestMajorSchemas, restriction)) {
      val msg = s"Illegal schema that violates compatibility restriction ($restriction) of this subject"
      log.warning(msg)
      Future(Conflict(ErrorResponse(409, msg)))
    } else latestMajorSchemas.headOption match {
      case Some(latestSchema) if latestSchema.avroSchema == avroSchema =>
        log.info("Schema did not created - Same definition as the latest one")
        Future.value(Ok(SchemaId(latestSchema.id))) // if posted schema conforms to the latest one, no need to create
      case _ =>
        val nextVersion = VersioningRule.nextVersion(avroSchema, latestMajorSchemas)
        SchemaClient.create(subject, nextVersion, avroSchema).map (id => Created(SchemaId(id)))
    }
  }} handle backendErrors


  /**
    * POST /subjects/(subject: string)/schema/lookup
    * BODY: Avro schema to lookup
    * Check if the posted schema is already exists under the specified subject
    * When it exists this returns the information of the latest schema.
    * @return
    */
  val lookup: Endpoint[Schema] = post(
    "subjects" :: path[String] :: "schema" :: "lookup" :: jsonBody[AvroSchema]
  ) { (subject: String, avroSchema: AvroSchema) =>
    SchemaClient.lookup(subject, avroSchema).map {
      case None => NotFound(ErrorResponse(404, "Schema Not Found"))
      case Some(schema) => Ok(schema)
    }
  } handle backendErrors


  /**
    * POST /subjects/(subject: string)/schema/lookupAll
    * BODY: Avro schema to lookup
    * Check if the posted schema is already exists under the specified subject
    * When existing this returns a list of schemas whose definition is the same as the posted one.
    * @return
    */
  val lookupAll: Endpoint[Seq[Schema]] = post(
    "subjects" :: path[String] :: "schema" :: "lookupAll" :: jsonBody[AvroSchema]
  ) { (subject: String, avroSchema: AvroSchema) =>
    SchemaClient.lookupAll(subject, avroSchema).map(Ok)
  } handle backendErrors


  /**
    * GET /schemas/ids/(subject: string)
    * Retrieve a schema definition by id
    */
  val readById: Endpoint[Schema] = get(
    "schemas" :: "ids" :: path[Long].should("be a natural number") {_ >= 0L}
  ) { id: Long =>
    SchemaClient.readById(id) map {
      case None => NotFound(ErrorResponse(404, "Schema Not Found"))
      case Some(schema) => Ok(schema)
    }
  } handle backendErrors



  /**
    * GET /subjects/(subject: string)/versions/(version: string)
    * Read a specific version of schema definition under the specified subject
    * The variable for version can take on one of the following format "latest", "v1", or "v1.0.0",
    * with the latter two representing the major version and the semantic version respectively.
    */
  val readByVersion: Endpoint[Schema] = get(
    "subjects" :: path[String] :: "versions" :: path[String]
  ) { (subject: String, version: String) =>
    parseAndReadByVersion(subject, version) map {
      case None => NotFound(ErrorResponse(404, "Schema Not Found"))
      case Some(schema) => Ok(schema)
    }
  } handle {
    case ex: IllegalArgumentException => UnprocessableEntity(ErrorResponse(422, ex.getMessage))
  } handle backendErrors


  /**
    * GET /subjects/(subject: string)/versions
    * Read registered versions under the specified subject
    */
  val readVersions: Endpoint[Seq[String]] = get(
    "subjects" :: path[String] :: "versions"
  ) { subject: String =>
    SchemaClient.readVersions(subject).map(_.map(_.toString)).map(Ok)
  } handle backendErrors


  /**
    * POST /compatibility/subjects/(subject: string)/versions/(version: string)
    * BODY: schema definition to test
    * Check if a posted schema is compatible with the specific version of schema under the specified subject
    */
  val checkCompatibility: Endpoint[Compatibility] = post(
    "compatibility" :: "subjects" :: path[String] :: "versions" :: path[String] :: jsonBody[AvroSchema]
  ) { (subject: String, version: String, postedSchema: AvroSchema) =>
    parseAndReadByVersion(subject, version) map {
      case None => NotFound(ErrorResponse(404, "Schema Not Found"))
      case Some(existingSchema) => Ok(Compatibility(isCompatible = CompatibilityUtil.isCompatible(postedSchema)(existingSchema.avroSchema)))
    }
  } handle {
    case ex: IllegalArgumentException => UnprocessableEntity(ErrorResponse(422, ex.getMessage))
  } handle backendErrors


  // utility to read the latest schema under the subject and compatibility restriction
  private def withLatestMajorSchemasAndRestriction[T](subject: String)(f: (Seq[Schema], SchemaCompatibility) => Future[T]) =
    SchemaClient.readLatestMajorSchemas(subject) join
      ConfigClient.readProperty(subject, RegistryConfig.Compatibility).map(_.getOrElse("NONE")).map(SchemaCompatibility.withName) flatMap {
      case (latestMajorSchemas: Seq[Schema], restriction: SchemaCompatibility) => f(latestMajorSchemas, restriction)
    }

}
