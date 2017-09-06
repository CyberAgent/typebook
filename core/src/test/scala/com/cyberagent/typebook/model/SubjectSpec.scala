package com.cyberagent.typebook.model

import org.scalatest._

import com.cyberagent.typebook.UnitTest

class SubjectSpec extends WordSpec with Matchers {

  "A Subject class" when {

    "all parameters are valid" should {
      "be constructed correctly" taggedAs UnitTest in {
        val subject = Subject("test-subject", Some("This is test"))
        subject.name should equal ("test-subject")
        subject.description.isDefined shouldBe true
        subject.description.get should equal ("This is test")
      }
    }

    "a `name` field is empty" should {
      "throw an IllegalArgumentException" taggedAs UnitTest in {
        an [IllegalArgumentException] should be thrownBy Subject("")
      }
    }

  }

}
