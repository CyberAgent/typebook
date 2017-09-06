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

package com.cyberagent.typebook.model

import org.apache.avro.SchemaParseException
import org.apache.avro.{Schema => AvroSchema}

import com.cyberagent.typebook.version.SemanticVersion


/**
  * This case class corresponds to a Schema of the Subject
  * `definition` must be a valid Avro schema otherwise this throws SchemaParseException
  * `subject` and `version` link `definition` to a specific Subject
  * @param subject
  * @param version
  * @param schema this is expected to be a valid Avro schema
  * @throws SchemaParseException this is thrown when the definition is invalid as a avro schema
  */
case class Schema(id: Id, subject: String, version: SemanticVersion, schema: String) {
  require(id > 0, "Id must be a natural number")
  require(subject != null && subject.nonEmpty, "subject must not be empty")
  require(schema != null, "definition must be a valid Avro Schema")
  val avroSchema: AvroSchema = new AvroSchema.Parser().parse(schema)
}

case class SchemaId(id: Id)