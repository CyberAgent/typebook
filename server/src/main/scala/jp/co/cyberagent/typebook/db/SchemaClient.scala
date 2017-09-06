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

import com.twitter.finagle.mysql._
import com.twitter.util.Future
import org.apache.avro.{Schema => AvroSchema}

import jp.co.cyberagent.typebook.model.Schema
import jp.co.cyberagent.typebook.version.SemanticVersion

object SchemaClient extends RaisingServerError {

  type Id = Long
  type AffectedRows = Long

  /**
    * Create a schema with the given parameters
    * schema definition is stored in a canonical form
    * @param subject
    * @param schema
    * @param client
    * @return
    */
  def create(subject: String, version: SemanticVersion, schema: AvroSchema)(implicit client: Client): Future[Id] = client.prepare(
      "INSERT INTO `schemas` (subject, major_version, minor_version, patch_version, definition) VALUES (?, ?, ?, ?, ?)"
    )(subject, version.major, version.minor, version.patch, schema.toString) map (result => withRaisingException(result) {
      result.asInstanceOf[OK].insertId
    })


  /**
    * Create a schema with the given parameters
    * schema definition is stored in a canonical form
    * @param subject
    * @param definition
    * @param client
    * @return
    */
  def create(subject: String, version: SemanticVersion, definition: String)(implicit client: Client): Future[Id] = create(
    subject, version, new AvroSchema.Parser().parse(definition)
  )


  /**
    * Read a avro schema with the given id
    * @param id
    * @param client
    * @return
    */
  def readById(id: Long)(implicit client: Client): Future[Option[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE id = ?"
  )(id) map (result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.headOption.map(convertToSchema)
  })

  /**
    * Read a specific version of avro schema under the specified subject
    * @param subject
    * @param version
    * @param client
    * @return
    */
  def readByVersion(subject: String, version: SemanticVersion)(implicit client: Client): Future[Option[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND major_version  = ? AND minor_version = ? AND patch_version = ?"
  )(subject, version.major, version.minor, version.patch) map (result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.headOption.map( convertToSchema )
  })


  /**
    * Read the latest version of schema under the given subject and major version
    * @param subject
    * @param majorVersion
    * @param client
    * @return
    */
  def readByVersion(subject: String, majorVersion: Int)(implicit client: Client): Future[Option[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND major_version = ? ORDER BY minor_version DESC, patch_version DESC"
  )(subject, majorVersion) map (result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.headOption.map( convertToSchema )
  })

  /**
    * Read the latest version of avro schema under the specified subject
    * @param subject
    * @param client
    * @return
    */
  def readByVersion(subject: String)(implicit client: Client): Future[Option[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? ORDER BY major_version DESC, minor_version DESC, patch_version DESC LIMIT 1"
  )(subject) map (result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.headOption.map( convertToSchema )
  })


  /**
    * Read all versions under the subject
    * @param subject
    * @param client
    * @return
    */
  def readVersions(subject: String)(implicit client: Client): Future[Seq[SemanticVersion]] = client.prepare(
    "SELECT major_version, minor_version, patch_version FROM `schemas` WHERE subject = ? ORDER BY major_version, minor_version, patch_version"
  )(subject) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map ( convertToSemanticVersion )
  })


  /**
    * Read all schemas under the subject in descending order (from newer)
    * @param subject
    * @param client
    * @return
    */
  def readSchemas(subject: String)(implicit client: Client): Future[Seq[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? ORDER BY major_version DESC, minor_version DESC, patch_version DESC"
  )(subject) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map( convertToSchema )
  })


  /**
    * Read schemas under the subject whose major version equals to passed one in descending order (from newer)
    * @param subject
    * @param majorVersion
    * @param client
    * @return
    */
  def readSchemas(subject: String, majorVersion: Int)(implicit client: Client): Future[Seq[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND major_version = ? ORDER BY minor_version DESC, patch_version DESC"
  )(subject, majorVersion) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map( convertToSchema )
  })


  /**
    * Read schemas under the subject whose major version and minor version equals to passed arguments in descending order (from newer)
    * @param subject
    * @param majorVersion
    * @param minorVersion
    * @param client
    * @return
    */
  def readSchemas(subject: String, majorVersion: Int, minorVersion: Int)(implicit client: Client): Future[Seq[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND major_version = ? AND minor_version = ? ORDER BY patch version DESC"
  )(subject, majorVersion, minorVersion) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map( convertToSchema )
  })


  /**
    * Read schemas under the subject whose major version is latest
    * @param subject
    * @param client
    * @return
    */
  def readLatestMajorSchemas(subject: String)(implicit client: Client): Future[Seq[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND major_version = (SELECT MAX(major_version) FROM `schemas` WHERE subject = ?) ORDER BY major_version DESC, minor_version DESC, patch_version DESC"
  )(subject, subject) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map( convertToSchema )
  })


  /**
    * Lookup the latest schema that has the same definition as the supplied definition under the specified subject
    * @param schema
    * @param client
    * @return
    */
  def lookup(schema: AvroSchema)(implicit client: Client): Future[Option[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE definition = ? ORDER BY major_version DESC, minor_version DESC, patch_version DESC LIMIT 1"
  )(schema.toString) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map(convertToSchema).headOption
  })

  /**
    * Lookup the latest schema that has the same definition as the supplied definition under the specified subject
    * @param definition
    * @param client
    * @return
    */
  def lookup(definition: String)(implicit client: Client): Future[Option[Schema]] = lookup(
    new AvroSchema.Parser().parse(definition)
  )

  /**
    * Lookup the latest schema that has the same definition as the supplied definition under the specified subject
    * @param subject
    * @param schema
    * @param client
    * @return
    */
  def lookup(subject: String, schema: AvroSchema)(implicit client: Client): Future[Option[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND definition = ? ORDER BY major_version DESC, minor_version DESC, patch_version DESC LIMIT 1"
  )(subject, schema.toString) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map(convertToSchema).headOption
  })

  /**
    * Lookup the latest schema that has the same definition as the supplied definition under the specified subject
    * @param subject
    * @param definition
    * @param client
    * @return
    */
  def lookup(subject: String, definition: String)(implicit client: Client): Future[Option[Schema]] = lookup(
    subject, new AvroSchema.Parser().parse(definition)
  )

  /**
    * Lookup a list of schemas that has the same definition as the supplied one
    * @param schema
    * @param client
    * @return
    */
  def lookupAll(schema: AvroSchema)(implicit client: Client): Future[Seq[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE definition = ? ORDER BY major_version DESC, minor_version DESC, patch_version DESC"
  )(schema.toString) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map(convertToSchema)
  })

  /**
    * Lookup a list of schemas that has the same definition as the supplied one
    * @param definition
    * @param client
    * @return
    */
  def lookupAll(definition: String)(implicit client: Client): Future[Seq[Schema]] = lookupAll(
    new AvroSchema.Parser().parse(definition)
  )

  /**
    * Lookup a list of schemas that has the same definition as the supplied one under the specified subject
    * @param subject
    * @param schema
    * @param client
    * @return
    */
  def lookupAll(subject: String, schema: AvroSchema)(implicit client: Client): Future[Seq[Schema]] = client.prepare(
    "SELECT * FROM `schemas` WHERE subject = ? AND definition = ? ORDER BY major_version DESC, minor_version DESC, patch_version DESC"
  )(subject, schema.toString) map ( result => withRaisingException(result) {
    result.asInstanceOf[ResultSet].rows.map(convertToSchema)
  })

  /**
    * Lookup a list of schemas that has the same definition as the supplied one under the specified subject
    * @param subject
    * @param definition
    * @param client
    * @return
    */
  def lookupAll(subject: String, definition: String)(implicit client: Client): Future[Seq[Schema]] = lookupAll(
    subject, new AvroSchema.Parser().parse(definition)
  )

  def convertToSemanticVersion(row: Row): SemanticVersion = {
    val IntValue(major) = row("major_version").get
    val IntValue(minor) = row("minor_version").get
    val IntValue(patch) = row("patch_version").get
    SemanticVersion(major, minor, patch)
  }

  def convertToSchema(row: Row): Schema = {
    val LongValue(id) = row("id").get
    val StringValue(subject) = row("subject").get
    val version = convertToSemanticVersion(row)
    val StringValue(schema) = row("definition").get
    Schema(id, subject, version, schema)
  }

}
