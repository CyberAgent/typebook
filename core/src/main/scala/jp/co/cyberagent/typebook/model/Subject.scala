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

import io.circe.generic.extras.ConfiguredJsonCodec

/**
  * @param name
  * @param description
  * @throws IllegalArgumentException this is thrown when the name is empty
  */
@ConfiguredJsonCodec case class Subject(name: String, description: Option[String] = None) {
  require(name != null && name.nonEmpty, "subject name must not be empty")
}

object Subject {
  final val Name = "name"
  final val Description = "description"
  object Field extends Enumeration {
    type Field = Value
    final val Name = Value(Subject.Name)
    final val Description = Value(Subject.Description)
  }
}

