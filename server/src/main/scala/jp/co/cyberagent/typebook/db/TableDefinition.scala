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
import com.twitter.logging.Logger
import com.twitter.util.{Await, Future}

private [typebook] object TableDefinition {

  final object Tables extends Enumeration {
    type Tables = Tables.Value
    val Subject = Value("subjects")
    val Schema = Value("schemas")
    val Config = Value("configs")
  }

  private val log = Logger.get(this.getClass)

  import Tables._
  final val tableNames: Seq[String] = Tables.values.map(_.toString).toSeq

  private final val SubjectTable =
    s"""
      | CREATE TABLE IF NOT EXISTS `${Subject.toString}` (
      |   name        VARCHAR(255)  NOT NULL,
      |   description TEXT          DEFAULT NULL,
      |   CONSTRAINT  pk_subject    PRIMARY KEY (name)
      | );
    """.stripMargin

  private final val SchemaTable =
    s"""
      | CREATE TABLE IF NOT EXISTS `${Schema.toString}` (
      |   id                BIGINT        AUTO_INCREMENT,
      |   subject           VARCHAR(255)  NOT NULL,
      |   major_version     INTEGER       NOT NULL,
      |   minor_version     INTEGER       NOT NULL,
      |   patch_version     INTEGER       NOT NULL,
      |   definition        TEXT          NOT NULL,
      |   UNIQUE(subject, major_version, minor_version, patch_version),
      |   CONSTRAINT fk_schema     FOREIGN KEY (subject) REFERENCES `${Subject.toString}`(name),
      |   CONSTRAINT pk_schema     PRIMARY KEY (id),
      |   INDEX (subject, major_version, minor_version, patch_version),
      |   INDEX (definition(255))
      | );
    """.stripMargin

  private final val ConfigTable =
    s"""
      | CREATE TABLE IF NOT EXISTS `${Config.toString}` (
      |   subject     VARCHAR(255)  NOT NULL,
      |   property    VARCHAR(255)  NOT NULL,
      |   value       VARCHAR(255)  NOT NULL,
      |   CONSTRAINT  fk_config     FOREIGN KEY (subject) REFERENCES `${Subject.toString}`(name),
      |   CONSTRAINT  pk_config     PRIMARY KEY (subject, property),
      |   INDEX (subject)
      | );
    """.stripMargin


  private[typebook] def initializeTables()(implicit client: Client): Unit =
    Await.result(
      createSubjectTable() flatMap { _ =>
        createSchemaTable() join createConfigTable()
      }
    )

  private def create(table: Tables.Tables, query: String)(implicit client: Client): Future[Unit] =
    client.query(query) map {
      case OK(_, _, _, warningCount, message) =>
        log.info(s"Table `${table.toString}` is successfully created with warning count $warningCount - $message")
      case Error(code, sqlState, message) =>
        throw ServerError(code, sqlState, s"An error occurred when creating ${table.toString} table - $message")
      case _ =>
        throw new Exception(s"Failed to create table `${table.toString}`")
    }

  private def createSubjectTable()(implicit client: Client): Future[Unit] = create(Subject, SubjectTable)
  private def createSchemaTable()(implicit client: Client): Future[Unit] = create(Schema, SchemaTable)
  private def createConfigTable()(implicit client: Client): Future[Unit] = create(Config, ConfigTable)
}
