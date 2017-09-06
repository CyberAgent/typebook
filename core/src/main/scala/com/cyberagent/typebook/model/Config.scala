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

import io.circe.generic.extras.ConfiguredJsonCodec

/**
  * This case class corresponds to a configuration of a Subject
  * All properties must not be empty otherwise this throws IllegalArgumentException
 *
  * @param subject
  * @param property
  * @param value
  * @throws IllegalArgumentException
  */
@ConfiguredJsonCodec case class Config(subject: String, property: String, value: String) {
  require(subject != null && subject.nonEmpty, "subject must not be empty")
  require(property != null && property.nonEmpty, "property must not be empty")
  require(value != null && value.nonEmpty, "value must not be empty")
}
