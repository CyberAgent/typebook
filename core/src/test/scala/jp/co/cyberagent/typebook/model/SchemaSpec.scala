package jp.co.cyberagent.typebook.model

import org.apache.avro.SchemaParseException
import org.scalatest._

import jp.co.cyberagent.typebook.UnitTest
import jp.co.cyberagent.typebook.version.SemanticVersion


object SchemaSpec {

  val definition: String =
    """
      |{
      |    "type" : "record",
      |    "name" : "userInfo",
      |    "namespace" : "my.example",
      |    "fields" : [{"name" : "age", "type" : "int"}]
      |}
    """.stripMargin

  val invalidAvroDefinition: String =
    """
      |{
      |    "type" : "record",
      |    "namespace" : "my.example.updated",
      |    "fields" : [{"name" : "age", "type" : "int"}]
      |}
    """.stripMargin

  val invalidJsonDefinition: String  =
    """{
      | "hoge":,,,
      |}
    """.stripMargin

}


class SchemaSpec extends WordSpec with Matchers {
  import SchemaSpec._

  "A Schema class" when {

    "parameters are valid" should {
      "be constructed correctly" taggedAs UnitTest in {
        val schema = Schema(1, "test-subject", SemanticVersion("v1.0.1"), definition)
        schema.id should equal (1)
        schema.subject should equal ("test-subject")
        schema.version.toString should equal ("v1.0.1")
        schema.schema should equal (definition)
      }
    }

    "any fields other than `schema` are invalid" should {
      "throw an IllegalArgumentException" taggedAs UnitTest in {
        an[IllegalArgumentException] should be thrownBy Schema(-1, "test-subject", SemanticVersion("v1.1.0"), definition)
        an[IllegalArgumentException] should be thrownBy Schema(1, null, SemanticVersion("v1.1.0"), definition)
        an[IllegalArgumentException] should be thrownBy Schema(1, "", SemanticVersion("v1.1.0"), definition)
        an[IllegalArgumentException] should be thrownBy Schema(1, "test-subject", SemanticVersion("v1.1.0"), null)
      }
    }

    "the `schema` field is invalid" should {
      "throw a SchemaParseException" taggedAs UnitTest in {
        an [SchemaParseException] should be thrownBy Schema(1, "test-subject", SemanticVersion("v1.1.0"), invalidJsonDefinition)
        an [SchemaParseException] should be thrownBy Schema(1, "test-subject", SemanticVersion("v1.1.0"), invalidAvroDefinition)
      }
    }
  }

}
