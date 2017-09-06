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

package jp.co.cyberagent.typebook.api

import com.twitter.finagle.mysql.ServerError
import com.twitter.io.Buf
import com.twitter.logging.Logger
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.finch._
import io.finch.Error._

import jp.co.cyberagent.typebook.model.ErrorResponse


trait ErrorHandling {

  import ErrorHandling._

  private val log = Logger.get(this.getClass)

  val pfLogging: PartialFunction[Throwable, Throwable] = {
    case t: Throwable =>
      log.error(t, "Internal server error")
      t
  }

  def backendErrors[T]: PartialFunction[Throwable, Output[T]] = pfLogging andThen {
    case notPresent: NotPresent => BadRequest(exceptionToErrorResponse(notPresent))
    case notParsed: NotParsed => UnprocessableEntity(exceptionToErrorResponse(notParsed))
    case notValid: NotValid => UnprocessableEntity(exceptionToErrorResponse(notValid))
    case serverError: ServerError => serverError.code match {
      case 1054 => BadRequest(ErrorResponse(400, s"Invalid Field (${serverError.code}) - ${serverError.message}"))
      case _ => InternalServerError(ErrorResponse(500, s"Error Code (${serverError.code}) - ${serverError.message}"))
    }
    case ex: Throwable => InternalServerError(ErrorResponse(500, ex.getMessage))
  }
}

object ErrorHandling {

  implicit val errorEncoder: Encode.Json[Exception] = Encode.json[Exception] { case (e, cs) =>
    Buf.ByteArray.Owned(errorToJson(e).toString.getBytes(cs.name))
  }

  def exceptionToErrorResponse(e: Exception): ErrorResponse = e match {
    case error: ErrorResponse => error
    case notPresent: NotPresent => ErrorResponse(400, notPresent.getMessage)
    case notParsed: NotParsed => ErrorResponse(422, notParsed.getMessage)
    case notValid: NotValid => ErrorResponse(422, notValid.getMessage)
    case ex => ErrorResponse(500, ex.getMessage)
  }

  def errorToJson(e: Exception): Json = exceptionToErrorResponse(e).asJson
}
