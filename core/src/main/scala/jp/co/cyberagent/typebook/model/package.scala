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

package jp.co.cyberagent.typebook

import scala.util.{Failure, Success}
import scala.util.control.Exception.allCatch

import io.circe._
import io.circe.generic.extras._
import io.circe.generic.semiauto._
import org.apache.avro.{Schema => AvroSchema}

import jp.co.cyberagent.typebook.compatibility.SchemaCompatibility
import jp.co.cyberagent.typebook.version.SemanticVersion

package object model {
  type Id = Long

  implicit val jsonCodecConfig: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val subjectDecoder: Decoder[Subject] = deriveDecoder[Subject].map( subject => subject.description match {
    case Some(desc) if desc.isEmpty => subject.copy(description = None)
    case _ => subject
  })

  implicit val schemaEncoder: Encoder[Schema] = Encoder.instance { schema => Json.obj(
    "id" -> Json.fromLong(schema.id),
    "subject" -> Json.fromString(schema.subject),
    "version" -> Json.fromString(schema.version.toString),
    "schema" -> Json.fromString(schema.schema)
  )}
  implicit val schemaDecoder: Decoder[Schema] = Decoder.instance { cursor =>
    (cursor.get[Long]("id"), cursor.get[String]("subject"), cursor.get[String]("version"), cursor.get[String]("schema")) match {
      case (Right(id), Right(subject), Right(version), Right(schema)) => Right(Schema(id, subject, SemanticVersion(version), schema))
      case _ => Left(DecodingFailure("Schema decoding failure", cursor.history))
    }
  }

  implicit val avroSchemaDecoder: Decoder[AvroSchema] = Decoder.instance { cursor =>
    (allCatch either new AvroSchema.Parser().parse(cursor.value.toString)).left.map { ex =>
      DecodingFailure(ex.getMessage, cursor.history)
    }
  }

  import RegistryConfig._
  implicit val registryConfigEncoder: Encoder[RegistryConfig] = Encoder.instance[RegistryConfig] { conf =>
    Json.obj(
      Compatibility -> Json.fromString(conf.compatibility.toString)
    )
  }

  implicit val registryConfigDecoder: Decoder[RegistryConfig] = Decoder.instance[RegistryConfig] { cursor =>
    cursor.get[String](Compatibility) match {
      case Left(ex) => Left(ex)
      case Right(compatibilityStr) => allCatch withTry SchemaCompatibility.withName(compatibilityStr) match {
        case Failure(ex) => Left(DecodingFailure(ex.getMessage, cursor.history))
        case Success(compatibility) => Right(RegistryConfig(compatibility = compatibility))
      }
    }
  }

}
