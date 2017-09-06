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

import com.twitter.io.Buf
import io.finch._

import com.cyberagent.typebook.db.SubjectClient.AffectedRows
import com.cyberagent.typebook.db.{DefaultMySQLBackend, MySQLBackend, SubjectClient}
import com.cyberagent.typebook.model._


object SubjectService extends SubjectServiceTrait with DefaultMySQLBackend {
  def apply() = create :+: readField :+: read :+:
                readAll :+: update :+: del
}

trait SubjectServiceTrait extends ErrorHandling { self: MySQLBackend =>

  type InsertId = Long

  /**
    * POST /subjects/(subject: string)
    * BODY => a description of this subject
    * Create a new subject
    * @return
    */
  val create: Endpoint[InsertId] = post(
    "subjects" :: path[String] :: stringBodyOption
  ).as[Subject] mapOutputAsync { subject =>
    SubjectClient.create(subject).map(Created(_).withHeader("Content-Type" -> "text/plain"))
  } handle backendErrors


  /**
    * GET /subjects/(subject: string)
    * Read a specific subject with the given name
    * @return
    */
  val read: Endpoint[Subject] = get(
    "subjects" :: path[String]
  ) { subjectName: String =>
    SubjectClient.read(subjectName) map {
      case Some(subject) => Ok(subject)
      case None => NotFound(ErrorResponse(404, "Subject Not Found"))
    }
  } handle backendErrors


  /**
    * GET /subjects/(subject: string)}?field={name|description}
    * Read a specific field of the specified subject with the given name
    * available values for the param `field` are  `name` and `description`
    * @return
    */
  val readField: Endpoint[Buf] = get(
    "subjects" :: path[String] :: paramExists("field")
  ) { (subject: String, field: String) =>
    SubjectClient.read(subject, field).map {
      case Some(value) => Ok(Buf.Utf8(value)).withHeader("Content-Type" -> "text/plain")
      case None => NotFound(ErrorResponse(404, s"Value of $field of subject $subject is not found"))
    }
  } handle backendErrors



  /**
    * GET /subjects
    * Read all subjects registered in this registry
    * @return
    */
  val readAll: Endpoint[Seq[String]] = get(
    "subjects"
  ) {
    SubjectClient.readAll("name").map(Ok)
  } handle backendErrors


  /**
    * PUT /subjects/(subject: string)
    * BODY(optional): updated description
    * Update a description of the specified subject
    * and returns the number of updated rows by this query
    * @return
    */
  val update: Endpoint[AffectedRows] = put(
    "subjects" :: path[String] :: stringBodyOption
  ).as[Subject] mapOutputAsync { subject =>
    SubjectClient.update(subject.name, subject.description).map(Ok(_).withHeader("Content-Type" -> "text/plain"))
  } handle backendErrors


  /**
    * DELETE /subjects/(subject: string)
    * Delete a specific name of subject and
    * returns the number of deleted rows by this query
    * @return
    */
  val del: Endpoint[AffectedRows] = delete(
    "subjects" :: path[String]
  ) { name: String =>
    SubjectClient.delete(name).map(Ok(_).withHeader("Content-Type" -> "text/plain"))
  } handle backendErrors

}
