package jp.co.cyberagent.typebook.version

import org.scalatest._
import org.scalatest.Matchers._

import jp.co.cyberagent.typebook.UnitTest
import jp.co.cyberagent.typebook.model.Schema

class VersionSpec extends WordSpec {


  "A SemanticVersion" when {

    "its format is valid" should {

      "be correctly constructed from string " taggedAs UnitTest in {
        val v1 = SemanticVersion("v0.10.9")
        v1.major should equal(0)
        v1.minor should equal(10)
        v1.patch should equal(9)
        v1.toSeq should contain theSameElementsAs Seq(0, 10, 9)
      }

      "be correctly constructed from integers" taggedAs UnitTest in {
        val v2 = SemanticVersion(1, 10, 8)
        v2.major should equal(1)
        v2.minor should equal(10)
        v2.patch should equal(8)
        v2.toString should equal("v1.10.8")
      }

      "be able to compare its  correctly" taggedAs UnitTest in {
        SemanticVersion("v1.0.1") > SemanticVersion("v0.11.8") shouldBe true
        SemanticVersion("v0.1.12") > SemanticVersion("v0.1.13") shouldBe false
        SemanticVersion("v2.11.8") == SemanticVersion("v2.11.8") shouldBe true
        SemanticVersion("v0.1.12") != SemanticVersion("v0.1.13") shouldBe true
      }

      "be able to upgrade its version correctly" taggedAs UnitTest in {
        val v1 = SemanticVersion("v0.10.9")
        v1.majorUpdatedVersion.toString should equal("v1.0.0")
        v1.minorUpdatedVersion.toString should equal("v0.11.0")
        v1.patchUpdatedVersion.toString should equal("v0.10.10")
      }
    }


    "its format is invalid" should {
      "throw an IllegalArgumentException" taggedAs UnitTest in {
        a[IllegalArgumentException] should be thrownBy SemanticVersion("v01.0.2")
        a[IllegalArgumentException] should be thrownBy SemanticVersion("v0.1.")
      }
    }
  }



  "A nextVersion method of VersionRule" when {

    import jp.co.cyberagent.typebook.compatibility.CompatibilitySpec._

    "the given schema set is empty" should {
      "return the v1.0.0" taggedAs UnitTest in {
        val actual = VersioningRule.nextVersion(parse(totallyDifferentSchema), Seq.empty[Schema])
        actual should equal (SemanticVersion("v1.0.0"))
      }
    }

    "the given schema is FullCompatible with the given schema set" should {
      "return the patch updated version" taggedAs UnitTest in {
        val schemas = Seq(
          Schema(1L, "test", SemanticVersion("v1.0.0"), originalSchema),
          Schema(2L, "test", SemanticVersion("v1.0.1"), compatiblyEvolvedSchema)
        )
        val actual = VersioningRule.nextVersion(parse(originalSchema), schemas)
        actual should equal (SemanticVersion("v1.0.2"))
      }
    }

    "the given schema is BackwardCompatible with the given schema set" should {
      "return the minor updated version" taggedAs UnitTest in {
        val schemas = Seq(
          Schema(1L, "test", SemanticVersion("v1.0.0"), originalSchema),
          Schema(2L, "test", SemanticVersion("v1.1.0"), incompatiblyEvolvedSchema),
          Schema(3L, "test", SemanticVersion("v1.1.1"), compatiblyEvolvedSchema)
        )
        val actual = VersioningRule.nextVersion(parse(originalSchema), schemas)
        actual should equal (SemanticVersion("v1.2.0"))
      }
    }

    "the given schema is NotCompatible with the given schema set" should {
      "return the major updated version" taggedAs UnitTest in {
        val schemas = Seq(
          Schema(1L, "test", SemanticVersion("v1.0.0"), originalSchema),
          Schema(2L, "test", SemanticVersion("v1.1.0"), incompatiblyEvolvedSchema),
          Schema(3L, "test", SemanticVersion("v1.1.1"), compatiblyEvolvedSchema)
        )

        val actual = VersioningRule.nextVersion(parse(totallyDifferentSchema), schemas)
        actual should equal (SemanticVersion("v2.0.0"))
      }
    }
  }
}
