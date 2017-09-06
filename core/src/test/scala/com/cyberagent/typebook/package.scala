package com.cyberagent

import org.apache.avro.Schema

package object typebook {
  def normalizeSchema(schemaDef: String): String = new Schema.Parser().parse(schemaDef).toString
}
