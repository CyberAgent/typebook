package jp.co.cyberagent.typebook.model

import org.scalatest._

import jp.co.cyberagent.typebook.UnitTest

class ConfigSpec extends WordSpec with Matchers {

  "A Config class" when {

    "all parameters are valid" should {
      "be constructed correctly" taggedAs UnitTest in {
        val config = Config("test-subject", "executor-memory", "4G")
        config.subject should equal("test-subject")
        config.property should equal("executor-memory")
        config.value should equal("4G")
      }
    }

    "its parameters are invalid" should {
      "throw an IllegalArgumentException" taggedAs UnitTest in {
        an [IllegalArgumentException] should be thrownBy Config(null, "executor-memory", "4G")
        an [IllegalArgumentException] should be thrownBy Config("", "executor-memory", "4G")
        an [IllegalArgumentException] should be thrownBy Config("test-subject", null, "4G")
        an [IllegalArgumentException] should be thrownBy Config("test-subject", "", "4G")
        an [IllegalArgumentException] should be thrownBy Config("test-subject", "executor-memory", null)
        an [IllegalArgumentException] should be thrownBy Config("test-subject", "executor-memory", "")
      }
    }

  }

}
