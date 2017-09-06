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

import scala.util.matching.Regex


/**
  * semantic version representation
  * @param major
  * @param minor
  * @param patch
  */
case class SemanticVersion(major: Int, minor: Int, patch: Int) extends Ordered[SemanticVersion] {

  override def compare(that: SemanticVersion): Int = (this.toSeq zip that.toSeq).find(el => el._1 != el._2) match {
    case Some(x) => x._1 - x._2
    case None => 0
  }
  def toSeq: Seq[Int] = Seq(major, minor, patch)
  def majorUpdatedVersion: SemanticVersion = copy(major = major + 1, minor = 0, patch = 0)
  def minorUpdatedVersion: SemanticVersion = copy(major = major, minor = minor + 1, patch = 0)
  def patchUpdatedVersion: SemanticVersion = copy(major = major, minor = minor, patch = patch + 1)

  override def toString: String = s"v$major.$minor.$patch"
}


object SemanticVersion {

  val regexp: Regex = "v((0|[1-9][0-9]*)\\.){2}(0|[1-9][0-9]*)".r

  def apply(str: String): SemanticVersion = {
    require(regexp.pattern.matcher(str).matches(), s"Invalid format in version representation - $str")
    val versionArray = str.substring(1).split('.').map(_.toInt)
    val major = versionArray(0)
    val minor = versionArray(1)
    val patch = versionArray(2)
    require(major >= 0 && minor >= 0 && patch >= 0, "every part must not be negative")
    SemanticVersion(major, minor, patch)
  }
}
