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

package jp.co.cyberagent.typebook.model

import scala.util.Try

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import jp.co.cyberagent.typebook.compatibility.SchemaCompatibility

/**
  * @param compatibility
  */
case class RegistryConfig(
  compatibility: SchemaCompatibility = SchemaCompatibility.NotCompatible
) {
  import RegistryConfig._

  def toConfigs(subject: String): Seq[Config] = Seq(
    Config(subject, RegistryConfig.CompatibilityProperty, compatibility.toString)
  )
  def toJson: Json = this.asJson
  def toMap: Map[String, String] = Map(
    CompatibilityProperty -> compatibility.toString
  )
}


object RegistryConfig {

  // Available properties of RegistryConfig
  final val CompatibilityProperty = "compatibility"
  final val Properties: Set[String] = Set(
    CompatibilityProperty
  )

  /**
    * RegistryConfig instance that have default values
    */
  lazy val default = RegistryConfig()

  /**
    * Check if the given `property` is valid RegistryConfig
    * @param property
    * @return
    */
  def isDefined(property: String): Boolean = Properties.contains(property)

  /**
    * Create RegistryConfig from Seq of Configs
    * Note that subject of each Config is discarded
    * @param configs
    * @return
    */
  def fromSeq(configs: Seq[Config]): Try[RegistryConfig] = {
    val filtered = configs.collect {
      case Config(_, property, value) if isDefined(property) => (property, value)
    }.toMap
    Try(RegistryConfig(
      compatibility = filtered.get(CompatibilityProperty).map(SchemaCompatibility.apply).getOrElse(default.compatibility)
    ))
  }

  /**
    * Create RegistryConfig from Json
    * @param json
    * @return
    */
  def fromJson(json: String): Decoder.Result[RegistryConfig] = parse(json).right.getOrElse(Json.Null).as[RegistryConfig]
}
