package com.cyberagent.typebook.compatibility

import org.apache.avro.{Schema => AvroSchema}
import org.scalatest._

import com.cyberagent.typebook.UnitTest
import com.cyberagent.typebook.model.Schema
import com.cyberagent.typebook.version.SemanticVersion

object CompatibilitySpec {

  val originalSchema =
    """
      |{"namespace": "example.avro",
      | "type": "record",
      | "name": "user",
      | "fields": [
      |     {"name": "name", "type": "string"},
      |     {"name": "favorite_number",  "type": "int"}
      | ]
      |}
    """.stripMargin

  val compatiblyEvolvedSchema =
    """
      |{"namespace": "example.avro",
      | "type": "record",
      | "name": "user",
      | "fields": [
      |     {"name": "name", "type": "string"},
      |     {"name": "favorite_number",  "type": "int"},
      |     {"name": "favorite_color", "type": "string", "default": "green"}
      | ]
      |}
    """.stripMargin


  val incompatiblyEvolvedSchema =
    """
      |{"namespace": "example.avro",
      | "type": "record",
      | "name": "user",
      | "fields": [
      |     {"name": "name", "type": "string"},
      |     {"name": "favorite_number",  "type": "int"},
      |     {"name": "favorite_color", "type": "string"}
      | ]
      |}
    """.stripMargin



  val totallyDifferentSchema =
    """
      |{"namespace": "example.avro",
      | "type": "record",
      | "name": "user",
      | "fields": [
      |     {"name": "age", "type": "int"},
      |     {"name": "gender",  "type": "string"}
      | ]
      |}
    """.stripMargin

  def parse(schema: String) = {
    val parser = new AvroSchema.Parser()
    parser.parse(schema)
  }

}


class CompatibilitySpec extends WordSpec with Matchers {

  import CompatibilitySpec._
  import SchemaCompatibility._

  "A method `isCompatible`" should {
    "be able to judge compatibility between schemas correctly" when {

      import SchemaConversion._

      "a compatible schema is given" taggedAs UnitTest in {
        CompatibilityUtil.isCompatible(parse(compatiblyEvolvedSchema))(parse(originalSchema)) shouldBe true
        parse(compatiblyEvolvedSchema) isCompatibleWith parse(originalSchema) shouldBe true
      }

      "an incompatible schema is given" taggedAs UnitTest in {
        CompatibilityUtil.isCompatible(parse(incompatiblyEvolvedSchema))(parse(originalSchema)) shouldBe false
        (parse(incompatiblyEvolvedSchema) isCompatibleWith parse(originalSchema)) shouldBe false
      }
    }
  }


  "A method `checkCompatibility" should {
    "be able to return correct compatibility" taggedAs UnitTest in {
      import SchemaConversion._
      CompatibilityUtil.calcCompatibility(parse(compatiblyEvolvedSchema))(parse(originalSchema)) should equal (FullCompatible)
      parse(incompatiblyEvolvedSchema).calcCompatibilityWith(parse(originalSchema)) should equal (ForwardCompatible)
      parse(originalSchema).calcCompatibilityWith(parse(incompatiblyEvolvedSchema)) should equal (BackwardCompatible)
      parse(totallyDifferentSchema).calcCompatibilityWith(parse(originalSchema)) should equal (NotCompatible)
    }
  }


  "A method `isStrongerThanOrEqualTo" should {
    "be able to compare the strength of compatibility correctly" taggedAs UnitTest in {

      CompatibilityRestriction.isStrongerThanOrEqualTo(NotCompatible)(NotCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(NotCompatible)(BackwardCompatible) shouldBe false
      CompatibilityRestriction.isStrongerThanOrEqualTo(NotCompatible)(ForwardCompatible) shouldBe false
      CompatibilityRestriction.isStrongerThanOrEqualTo(NotCompatible)(FullCompatible) shouldBe false

      CompatibilityRestriction.isStrongerThanOrEqualTo(BackwardCompatible)(NotCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(BackwardCompatible)(BackwardCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(BackwardCompatible)(ForwardCompatible) shouldBe false
      CompatibilityRestriction.isStrongerThanOrEqualTo(BackwardCompatible)(FullCompatible) shouldBe false

      CompatibilityRestriction.isStrongerThanOrEqualTo(ForwardCompatible)(NotCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(ForwardCompatible)(BackwardCompatible) shouldBe false
      CompatibilityRestriction.isStrongerThanOrEqualTo(ForwardCompatible)(ForwardCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(ForwardCompatible)(FullCompatible) shouldBe false

      CompatibilityRestriction.isStrongerThanOrEqualTo(FullCompatible)(NotCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(FullCompatible)(BackwardCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(FullCompatible)(ForwardCompatible) shouldBe true
      CompatibilityRestriction.isStrongerThanOrEqualTo(FullCompatible)(FullCompatible) shouldBe true
    }
  }


  "A method `isWeakerThanOrEqualTo`" should {
    "be able to compare the strength of compatibility correctly" taggedAs UnitTest in {

      CompatibilityRestriction.isWeakerThanOrEqualTo(NotCompatible)(NotCompatible) shouldBe true
      CompatibilityRestriction.isWeakerThanOrEqualTo(NotCompatible)(BackwardCompatible) shouldBe true
      CompatibilityRestriction.isWeakerThanOrEqualTo(NotCompatible)(ForwardCompatible) shouldBe true
      CompatibilityRestriction.isWeakerThanOrEqualTo(NotCompatible)(FullCompatible) shouldBe true

      CompatibilityRestriction.isWeakerThanOrEqualTo(BackwardCompatible)(NotCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(BackwardCompatible)(BackwardCompatible) shouldBe true
      CompatibilityRestriction.isWeakerThanOrEqualTo(BackwardCompatible)(ForwardCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(BackwardCompatible)(FullCompatible) shouldBe true

      CompatibilityRestriction.isWeakerThanOrEqualTo(ForwardCompatible)(NotCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(ForwardCompatible)(BackwardCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(ForwardCompatible)(ForwardCompatible) shouldBe true
      CompatibilityRestriction.isWeakerThanOrEqualTo(ForwardCompatible)(FullCompatible) shouldBe true

      CompatibilityRestriction.isWeakerThanOrEqualTo(FullCompatible)(NotCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(FullCompatible)(BackwardCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(FullCompatible)(ForwardCompatible) shouldBe false
      CompatibilityRestriction.isWeakerThanOrEqualTo(FullCompatible)(FullCompatible) shouldBe true

    }
  }


  "A string representation of SchemaCompatibility" taggedAs UnitTest in {

    NotCompatible.toString should equal ("NONE")
    s"$NotCompatible" should equal ("NONE")

    ForwardCompatible.toString should equal ("FORWARD")
    s"$ForwardCompatible" should equal ("FORWARD")

    BackwardCompatible.toString should equal ("BACKWARD")
    s"$BackwardCompatible" should equal ("BACKWARD")

    FullCompatible.toString should equal ("FULL")
    s"$FullCompatible" should equal ("FULL")

  }


  "A method `checkCompatibility`" should {
    "be return true" when {
      "a given schema obeys given compatibility restriction with a passed schema set" taggedAs UnitTest in {
        val schemas = Seq(
          Schema(1L, "test", SemanticVersion("v1.0.1"), compatiblyEvolvedSchema),
          Schema(2L, "test", SemanticVersion("v1.0.0"), originalSchema)
        )
        CompatibilityUtil.checkCompatibility(parse(incompatiblyEvolvedSchema), schemas, ForwardCompatible) shouldBe true
        CompatibilityUtil.checkCompatibility(parse(incompatiblyEvolvedSchema), schemas, NotCompatible) shouldBe true
        CompatibilityUtil.checkCompatibility(parse(totallyDifferentSchema), schemas, NotCompatible) shouldBe true
        CompatibilityUtil.checkCompatibility(parse(totallyDifferentSchema), Seq.empty[Schema], FullCompatible) shouldBe true
      }
    }
    "be return false" when {
      "a given schema violates given compatibility restriction with a passed schema set" taggedAs UnitTest in {
        val schemas = Seq(
          Schema(1L, "test", SemanticVersion("v1.0.1"), compatiblyEvolvedSchema),
          Schema(2L, "test", SemanticVersion("v1.0.0"), originalSchema)
        )
        CompatibilityUtil.checkCompatibility(parse(incompatiblyEvolvedSchema), schemas, FullCompatible) shouldBe false
        CompatibilityUtil.checkCompatibility(parse(incompatiblyEvolvedSchema), schemas, BackwardCompatible) shouldBe false
        CompatibilityUtil.checkCompatibility(parse(totallyDifferentSchema), schemas, ForwardCompatible) shouldBe false
        CompatibilityUtil.checkCompatibility(parse(totallyDifferentSchema), schemas, BackwardCompatible) shouldBe false
        CompatibilityUtil.checkCompatibility(parse(totallyDifferentSchema), schemas, FullCompatible) shouldBe false
      }
    }
  }
}
