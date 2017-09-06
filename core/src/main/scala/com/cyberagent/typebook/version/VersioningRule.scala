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

package com.cyberagent.typebook.version

import org.apache.avro.{Schema => AvroSchema}

import com.cyberagent.typebook.compatibility.CompatibilityUtil
import com.cyberagent.typebook.compatibility.SchemaCompatibility.{FullCompatible, SchemaCompatibility}
import com.cyberagent.typebook.compatibility.SchemaCompatibility._
import com.cyberagent.typebook.model.Schema

object VersioningRule {

  /**
    * calculate next semantic version based on a schema definition to register
    * and a set of the latest major version of schemas
    * @param schema schema definition to version
    * @param latestMajorSchemas a set of the latest major version of schemas
    * @return
    */
  def nextVersion(schema: AvroSchema, latestMajorSchemas: Seq[Schema]): SemanticVersion = {
    val schemas = latestMajorSchemas.sortWith{ (s1, s2) => s1.version > s2.version } // sort in descending order by version
    schemas.headOption match {
      case None => SemanticVersion("v1.0.0") // if no existing schema, this is the first version
      case Some(latestSchema) =>
        schemas.foldLeft((latestSchema, FullCompatible)) { (acc, iterator) => // find the schema with lowest compatibility
          val compatibility = CompatibilityUtil.calcCompatibility(schema)(iterator.avroSchema)
          if (isLowerCompatibility(compatibility)(acc._2)) (iterator, compatibility) else acc
        } match {
          case (_, NotCompatible) | (_, ForwardCompatible) => latestSchema.version.majorUpdatedVersion // when NotCompatible or ForwardCompatible schema is found in the latest major version
          case (lowestCompatibilitySchema, BackwardCompatible) if lowestCompatibilitySchema.version.minor == latestSchema.version.minor => latestSchema.version.minorUpdatedVersion
          case _ => latestSchema.version.patchUpdatedVersion
        }
    }
  }

  /**
    * check if `target`
    * @param target
    * @param comparison
    * @return
    */
  private def isLowerCompatibility(target: SchemaCompatibility)(comparison: SchemaCompatibility): Boolean = comparison match {
    case NotCompatible | ForwardCompatible => false
    case BackwardCompatible => target == NotCompatible || target == ForwardCompatible
    case FullCompatible => target == BackwardCompatible || target == ForwardCompatible || target == NotCompatible
  }
}
