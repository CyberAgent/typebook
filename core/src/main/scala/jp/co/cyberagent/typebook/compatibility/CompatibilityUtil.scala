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

package jp.co.cyberagent.typebook.compatibility

import org.apache.avro.{Schema => AvroSchema, SchemaCompatibility => AvroSchemaCompatibility}
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType._

import jp.co.cyberagent.typebook.model.Schema


/**
  * 4 types of Avro schema compatibility
  */
sealed trait SchemaCompatibility
object SchemaCompatibility  {
  final case object NotCompatible extends SchemaCompatibility {
    override def toString: String = "NONE"
  }
  final case object ForwardCompatible extends SchemaCompatibility {
    override def toString: String = "FORWARD"
  }
  final case object BackwardCompatible extends SchemaCompatibility {
    override def toString: String = "BACKWARD"
  }
  final case object FullCompatible extends SchemaCompatibility {
    override def toString: String = "FULL"
  }

  def apply(name: String): SchemaCompatibility = name match {
    case "NONE" => NotCompatible
    case "FORWARD" => ForwardCompatible
    case "BACKWARD" => BackwardCompatible
    case "FULL" => FullCompatible
    case _ => throw new NoSuchElementException(s"Value for $name not found")
  }
}


/**
  * Avro schema compatibility checking utility
  */
object CompatibilityUtil {
  import SchemaCompatibility._

  /**
    * calculate the compatibility between `target` and `comparison`
    * BackwardCompatible means that a data encoded by `comparison` can be decoded with `target` but not vice versa
    * ForwardCompatible means that a data encoded by `target` can be decoded with `comparison` but not vice versa
    * FullCompatible means that a data encoded by either schema can be decoded by another one
    * NotCompatible means that a data encoded by either schema totally cannot be decoded by another one
    * @param target
    * @param comparison
    * @return
    */
  def calcCompatibility(target: AvroSchema)(comparison: AvroSchema): SchemaCompatibility =
    (isCompatible(target)(comparison), isCompatible(comparison)(target)) match {
      case (true, false) => BackwardCompatible
      case (false, true) => ForwardCompatible
      case (true, true) => FullCompatible
      case (false, false) => NotCompatible
    }

  /**
    * calculate the compatibility of `target` with `comparison`
    * BackwardCompatible means that a data encoded by `comparison` can be decoded with `target` but not vice versa
    * ForwardCompatible means that a data encoded by `target` can be decoded with `comparison` but not vice versa
    * FullCompatible means that a data encoded by either schema can be decoded by another one
    * NotCompatible means that a data encoded by either schema totally cannot be decoded by another one
    * @param target
    * @param comparison
    * @return
    */
  def calcCompatibility(target: String)(comparison: String): SchemaCompatibility =
    calcCompatibility(new AvroSchema.Parser().parse(target))(new AvroSchema.Parser().parse(comparison))


  /**
    * check if the given `target` schema obeys compatibility `restriction` comparing with all given `existingSchemas`
    * @param target
    * @param existingSchemas
    * @param restriction
    * @return
    */
  def checkCompatibility(target: AvroSchema, existingSchemas: Seq[Schema], restriction: SchemaCompatibility): Boolean =
    existingSchemas.isEmpty || existingSchemas.forall { schema =>
      CompatibilityRestriction.isStrongerThanOrEqualTo(calcCompatibility(target)(schema.avroSchema))(restriction)
    }
  

  /**
    * this returns true when a data encoded by `comparison` can be decoded with `target`
    * @param target
    * @param comparison
    * @return
    */
  def isCompatible(target: AvroSchema)(comparison: AvroSchema): Boolean =
    AvroSchemaCompatibility.checkReaderWriterCompatibility(target, comparison).getType == COMPATIBLE

  /**
    * this returns true when a data encoded by `comparison` can be decoded with `target`
    * @param target
    * @param comparison
    * @return
    */
  def isCompatible(target: String)(comparison: String): Boolean =
    isCompatible(new AvroSchema.Parser().parse(target))(new AvroSchema.Parser().parse(comparison))

}


/**
  * Implicit conversion to add compatibility checking functionality to `Schema`
  */
object SchemaConversion {
  implicit class SchemaConversion(val target: AvroSchema) extends AnyVal {
    def isCompatibleWith(comparison: AvroSchema): Boolean = CompatibilityUtil.isCompatible(target)(comparison)
    def calcCompatibilityWith(comparison: AvroSchema): SchemaCompatibility = CompatibilityUtil.calcCompatibility(target)(comparison)
  }
}


/**
  * Utility for checking compatibility restriction of `Config`
  */
object CompatibilityRestriction {
  import SchemaCompatibility._
  /**
    * Check if `target` is a stronger restriction than `comparison` or the both are equal
    * @param target
    * @param comparison
    * @return
    */
  def isStrongerThanOrEqualTo(target: SchemaCompatibility)(comparison: SchemaCompatibility): Boolean = comparison match {
    case NotCompatible => true
    case ForwardCompatible => target == ForwardCompatible || target == FullCompatible
    case BackwardCompatible => target == BackwardCompatible || target == FullCompatible
    case FullCompatible => target == FullCompatible
  }


  /**
    * Check if `target` is a weaker restriction than `comparison` or the both are equal
    * @param target
    * @param comparison
    * @return
    */
  def isWeakerThanOrEqualTo(target: SchemaCompatibility)(comparison: SchemaCompatibility): Boolean = comparison match {
    case NotCompatible => target == NotCompatible
    case ForwardCompatible => target == ForwardCompatible || target == NotCompatible
    case BackwardCompatible => target == BackwardCompatible || target == NotCompatible
    case FullCompatible => true
  }
}
