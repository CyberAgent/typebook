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

import jp.co.cyberagent.typebook.model.Subject


object SubjectClient extends RaisingServerError {

  type InsertId = Long
  type AffectedRows = Long

  /**
    * Create a subject with given parameters
    * @param subject
    * @param client
    * @return Future(0) when succeeded or throwing ServerError otherwise
    */
  def create(subject: Subject)(implicit client: Client): Future[InsertId] =
    client.prepare(
      "INSERT INTO `subjects` (name, description) VALUES (?, ?)"
    )(
      subject.name,
      subject.description.map(Parameter.wrap(_)).getOrElse(Parameters.nullParameter) // to set nullParameter when the given description is None or Some(null)
    ) map ( result => withRaisingException(result) {
      result.asInstanceOf[OK].insertId
    })

  /**
    * Read a subject with the given name
    * @param subjectName
    * @param client
    * @return Subject when existing otherwise None. When an error occurs ServerError will be thrown.
    */
  def read(subjectName: String)(implicit client: Client): Future[Option[Subject]] =
    client.prepare(
      "SELECT * FROM `subjects` WHERE name = ?"
    )(subjectName) map ( result => withRaisingException(result) {
      result.asInstanceOf[ResultSet].rows.map(convertToEntity).headOption
    })

  /**
    * Read a specific field of a Subject with the given name
    * @param subjectName
    * @param field ("name" or "description")
    * @param client
    * @return the value of specific field when the subject exists otherwise this returns None. When an error occurs ServerError will be thrown.
    */
  def read(subjectName: String, field: String)(implicit client: Client): Future[Option[String]] =
    client.prepare(
      s"SELECT $field FROM `subjects` WHERE name = ?"
    )(subjectName) map ( result => withRaisingException(result) {
      result.asInstanceOf[ResultSet].rows.headOption flatMap ( row => row(field).get match {
        case StringValue(value) => Some(value)
        case _ => None
      })
    })

  /**
    * Read all subjects registered in this registry.
    * @param client
    * @return
    */
  def readAll()(implicit client: Client): Future[Seq[Subject]] = client.select(
      "SELECT * FROM `subjects`"
    )(convertToEntity)


  /**
    * Read a specific field of all subjects
    * @param field ("name" or "description")
    * @param client
    * @return
    */
  def readAll(field: String)(implicit client: Client): Future[Seq[String]] =
    client.select(
      s"SELECT $field FROM `subjects` WHERE $field IS NOT NULL"
    ) ( row => row(field).get match {
      case StringValue(str) => str
      case _ => ""
    })

  /**
    * Update a description of the specified subject
    * @param subjectName
    * @param description
    * @param client
    * @return the number of updated rows when succeeded or throwing ServerError otherwise
    */
  def update(subjectName: String, description: Option[String])(implicit client: Client): Future[AffectedRows] =
    client.prepare(
      "UPDATE `subjects` SET description = ? WHERE name = ?"
    )(
      description.map(Parameter.wrap(_)).getOrElse(Parameters.nullParameter), // to set nullParameter when the given description is None or Some(null)
      subjectName
    ) map ( result => withRaisingException(result) {
      result.asInstanceOf[OK].affectedRows
    })

  /**
    * Delete the specified subject
    * @param subjectName
    * @param client
    * @return the number of updated rows when succeeded or throwing ServerError otherwise
    */
  def delete(subjectName: String)(implicit client: Client): Future[AffectedRows] =
    client.prepare(
      "DELETE FROM `subjects` WHERE name = ?"
    )(subjectName) map ( result => withRaisingException(result) {
      result.asInstanceOf[OK].affectedRows
    })


  def convertToEntity(row: Row): Subject = {
    val StringValue(name) = row("name").get
    val description = row("description").flatMap {
      case StringValue(desc) => Some(desc)
      case _ => None
    }
    Subject(name, description)
  }
}
