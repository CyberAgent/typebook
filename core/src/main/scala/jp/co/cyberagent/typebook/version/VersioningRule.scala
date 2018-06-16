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

package jp.co.cyberagent.typebook.version

import org.apache.avro.{Schema => AvroSchema}

import jp.co.cyberagent.typebook.compatibility.CompatibilityUtil
import jp.co.cyberagent.typebook.compatibility.SchemaCompatibility
import jp.co.cyberagent.typebook.compatibility.SchemaCompatibility._
import jp.co.cyberagent.typebook.model.Schema

object VersioningRule {

  /**
    * Calculate the version for given `schema` comply with the following policy.
    *   1. Schemas under the same major version should have at least backward compatibility to ensure that
    *      the latest schema is applicable to all datasets under the same major version.
    *   2. Schemas under the same minor version should have full compatibility.
    * @param schema schema definition to version
    * @param latestMajorSchemas a set of the latest major version of schemas
    * @return
    */
  def nextVersion(schema: AvroSchema)(latestMajorSchemas: Seq[Schema]): SemanticVersion = {
    val comparisons = latestMajorSchemas.sortWith{ (s1, s2) => s1.version > s2.version } // sort in descending order by version
    comparisons.headOption match {
      case None => SemanticVersion(1, 0, 0) // if no existing schema, this is the first version
      case Some(latest) =>                  // otherwise find the version which has the lowest compatibility
        comparisons.foldLeft[(SemanticVersion, SchemaCompatibility)]((latest.version, FullCompatible)) { (lowest, comparison) =>
          val compat = CompatibilityUtil.calcCompatibility(schema)(comparison.avroSchema)
          if (isLowerCompatibility(compat)(lowest._2)) (comparison.version, compat) else lowest
        } match { // determine the next version
          case (_, NotCompatible) | (_, ForwardCompatible) => latest.version.majorUpdatedVersion                            // policy 1
          case (lowest, BackwardCompatible) if lowest.minor == latest.version.minor => latest.version.minorUpdatedVersion   // policy 2
          case _ => latest.version.patchUpdatedVersion                                                                      // policy 2
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
