package com.cyberagent.typebook

import com.twitter.io.Buf
import com.twitter.util.Duration
import io.finch.{Application, Input, Text}
import org.apache.avro.{Schema => AvroSchema}
import org.scalatest._

import com.cyberagent.typebook.api.{ConfigServiceTrait, SchemaServiceTrait, SubjectServiceTrait}
import com.cyberagent.typebook.model._
import com.cyberagent.typebook.version.SemanticVersion

import java.util.concurrent.TimeUnit
import scala.language.postfixOps


object SchemaServiceIntegrationSpec {
  val schemaSubject: String = "test-subject"
  val schemaSubject2: String = "test-subject2"
  val schemaDefinition: String = s"""
    |  {"namespace": "example.avro",
    |   "type": "record",
    |   "name": "User",
    |   "fields": [
    |     {"name": "name", "type": "string"},
    |     {"name": "favorite_number",  "type": ["int", "null"]},
    |     {"name": "favorite_color", "type": ["string", "null"]}
    |   ]
    |  }
  """.stripMargin
  val backwardCompatibleSchemaDefinition: String = s"""
    |  {"namespace": "example.avro",
    |   "type": "record",
    |   "name": "User",
    |   "fields": [
    |     {"name": "name", "type": "string"},
    |     {"name": "favorite_number",  "type": ["int", "null"]}
    |   ]
    |  }
  """.stripMargin
  val forwardCompatibleSchemaDefinition: String =
    s"""
         {"namespace": "example.avro",
       |   "type": "record",
       |   "name": "User",
       |   "fields": [
       |     {"name": "name", "type": "string"},
       |     {"name": "favorite_number",  "type": ["int", "null"]},
       |     {"name": "favorite_color", "type": ["string", "null"]},
       |     {"name": "favorite_person", "type": "string"}
       |   ]
       |  }
     """.stripMargin
  val fullCompatibleSchemaDefinition: String =
    s"""
         {"namespace": "example.avro",
       |   "type": "record",
       |   "name": "User",
       |   "fields": [
       |     {"name": "name", "type": "string"},
       |     {"name": "favorite_number",  "type": ["int", "null"]},
       |     {"name": "favorite_color", "type": ["string", "null"]},
       |     {"name": "favorite_person", "type": "string", "default": "Lupin"}
       |   ]
       |  }
     """.stripMargin
  val invalidDefinition: String =
    """
      |{"namespace: "example.avro",
      |   "type": "record",
      |   "name": "User",
      |   "fields" [
      |     {"name": "name", "type": "string"},
      |     {"name": "favorite_number",  "type": ["int", "null"]},
      |     {"name": "favorite_color", "type
      |   ]
      |  }
    """.stripMargin
}


class SchemaServiceIntegrationSpec extends FlatSpec with StorageBackend with StorageClientUtil with Matchers {

  import SchemaServiceIntegrationSpec._
  import SubjectServiceIntegrationSpec._
  val awaitTime = Duration(10, TimeUnit.SECONDS)

  def parse(definition: String): AvroSchema = new AvroSchema.Parser().parse(definition)


  // FIXME It might be better to use WordSpec but there is no way to call dockerComposition out of "in" clause.
  // Another way is call it in each "in" clause but it makes test too slow...
  // For those reasons, gave up to use WordSpec and adopt FlatSpec for IntegrationTest

  "A SchemaService" should "correctly serve CRUD requests" taggedAs IntegrationTest in {
    withRule(dockerComposition("schema-service")) { dc =>
      withDbClient(dc) { client =>


        import io.circe.generic.auto._
        import io.finch.circe._

        // Service API to test
        object TestSchemaService extends TestMySqlBackend(client) with SchemaServiceTrait

        // For subject registration
        object TestSubjectService extends TestMySqlBackend(client) with SubjectServiceTrait

        // For setting compatibility restriction
        object TestConfigService extends TestMySqlBackend(client) with ConfigServiceTrait


        // Normal: create and read a schema
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject to register a schema with
          TestSubjectService.create( Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          val schemaId = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"Created schema id is $schemaId")
          schemaId.id should equal(1L)

          val schema = TestSchemaService.readById(
            Input.get(s"/schemas/ids/${schemaId.id}")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"Created schema is $schema")

          schema.schema should equal(parse(schemaDefinition).toString)
        }


        // Normal: Create multiple schemas and read versions under the subject then read each version of schema
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          val schemaId1 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId2 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(backwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId3 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(forwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          schemaId1.id should equal(1L)
          schemaId2.id should equal(2L)
          schemaId3.id should equal(3L)
          log.info("before versions")
          val versions = TestSchemaService.readVersions(
            Input.get(s"/subjects/$schemaSubject/versions")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"created versions are $versions")
          versions should equal(Seq("v1.0.0", "v1.1.0", "v2.0.0"))

          val schemaV1 = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/v1.0.0")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"SchemaV1 is $schemaV1")
          schemaV1.subject should equal(schemaSubject)
          schemaV1.version should equal(SemanticVersion("v1.0.0"))
          schemaV1.schema should equal(parse(schemaDefinition).toString)

          val schemaV11 = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/v1.1.0")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"SchemaV11 is $schemaV11")
          schemaV11.subject should equal(schemaSubject)
          schemaV11.version should equal(SemanticVersion("v1.1.0"))
          schemaV11.schema should equal(parse(backwardCompatibleSchemaDefinition).toString)

          val schemaV2 = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/v2.0.0")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"SchemaV12 is $schemaV2")
          schemaV2.subject should equal(schemaSubject)
          schemaV2.version should equal(SemanticVersion("v2.0.0"))
          schemaV2.schema should equal(parse(forwardCompatibleSchemaDefinition).toString)


          val v1LatestSchema = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/v1")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"the latest schema with major version 1 is $v1LatestSchema")
          v1LatestSchema should equal(schemaV11)

          val latestSchema = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/latest")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"the latest schema is $latestSchema")

          latestSchema should equal(schemaV2)
        }


        // Normal: create multiple schemas and lookup each schema by definition
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)
          // create another test subject
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject2").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          val schemaId1 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId2 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(fullCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId3 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject2/versions").withBody[Application.Json](Buf.Utf8(backwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId4 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject2/versions").withBody[Application.Json](Buf.Utf8(forwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val ids = Seq(schemaId1.id, schemaId2.id, schemaId3.id, schemaId4.id)
          log.info(s"ids are $ids")
          ids should equal(Seq(1L, 2L, 3L, 4L))


          val found1 = TestSchemaService.lookup(
            Input.post(s"/subjects/$schemaSubject/schema/lookup").withBody[Application.Json](Buf.Utf8(parse(schemaDefinition).toString(true))) // lookup by pretty format to differentiate from the one that used when creating
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"original schema is found on $found1")
          found1.subject should equal(schemaSubject)
          found1.version should equal(SemanticVersion("v1.0.0"))

          val found2 = TestSchemaService.lookup(
            Input.post(s"/subjects/$schemaSubject/schema/lookup").withBody[Application.Json](Buf.Utf8(fullCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"full compatible schema is found on $found2")
          found2.subject should equal(schemaSubject)
          found2.version should equal(SemanticVersion("v1.0.1"))

          val found3 = TestSchemaService.lookup(
            Input.post(s"/subjects/$schemaSubject2/schema/lookup").withBody[Application.Json](Buf.Utf8(backwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"backward compatible schema is found on $found3")
          found3.subject should equal(schemaSubject2)
          found3.version should equal(SemanticVersion("v1.0.0"))

          val found4 = TestSchemaService.lookup(
            Input.post(s"/subjects/$schemaSubject2/schema/lookup").withBody[Application.Json](Buf.Utf8(forwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"forward compatible schema is found on $found4")
          found4.subject should equal(schemaSubject2)
          found4.version should equal(SemanticVersion("v2.0.0"))


          // lookup on the subject that the posted schema is not registered
          // it should return 404
          val tryOutput = TestSchemaService.lookup(
            Input.post(s"/subjects/$schemaSubject/schema/lookup").withBody[Application.Json](Buf.Utf8(forwardCompatibleSchemaDefinition))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"the result of looking up not existing schema is $tryOutput")
          tryOutput.status.code should equal (404)
        }


        // Normal create multiple schemas. Two of those have the same definition.
        // Lookup all schemas that has the same definition as the posted one
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          val schemaId1 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId2 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(fullCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val schemaId3 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val found1 = TestSchemaService.lookupAll(
            Input.post(s"/subjects/$schemaSubject/schema/lookupAll").withBody[Application.Json](Buf.Utf8(parse(schemaDefinition).toString(true)))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"original schemas are found as follows $found1")
          found1 should contain theSameElementsAs Seq(
            Schema(3L, schemaSubject, SemanticVersion("v1.0.2"), normalizeSchema(schemaDefinition)),
            Schema(1L, schemaSubject, SemanticVersion("v1.0.0"), normalizeSchema(schemaDefinition))
          )

          val found2 = TestSchemaService.lookupAll(
            Input.post(s"/subjects/$schemaSubject/schema/lookupAll").withBody[Application.Json](Buf.Utf8(parse(fullCompatibleSchemaDefinition).toString(true)))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"a fullCompatible schema is found as follows $found2")
          found2 should contain theSameElementsAs Seq(
            Schema(2L, schemaSubject, SemanticVersion("v1.0.1"), normalizeSchema(fullCompatibleSchemaDefinition))
          )
        }


        // Normal: Check compatibility between schemas
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Application.Json](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          TestSchemaService.create(
            Input.post(s"/subjects/$subjectName/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime)

          val backwardIsCompatible = TestSchemaService.checkCompatibility(
            Input.post(s"/compatibility/subjects/$subjectName/versions/v1.0.0").withBody[Application.Json](Buf.Utf8(backwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"'backward is compatible' is ${backwardIsCompatible.isCompatible}")
          backwardIsCompatible.isCompatible shouldBe true

          val forwardIsCompatible = TestSchemaService.checkCompatibility(
            Input.post(s"/compatibility/subjects/$subjectName/versions/latest").withBody[Application.Json](Buf.Utf8(forwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"'forward is compatible' is ${forwardIsCompatible.isCompatible}")
          forwardIsCompatible.isCompatible shouldBe false

          val fullIsCompatible = TestSchemaService.checkCompatibility(
            Input.post(s"/compatibility/subjects/$subjectName/versions/latest").withBody[Application.Json](Buf.Utf8(fullCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"'full is compatible' is ${fullIsCompatible.isCompatible}")
          fullIsCompatible.isCompatible shouldBe true
        }



        // Normal: set a schema compatibility restriction and try to create valid and invalid schemas
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)
          // set a restriction that only allow to register schemas that has backward compatibility
          TestConfigService.setProperty(Input.put(s"/config/$schemaSubject/properties/compatibility").withBody[Text.Plain](Buf.Utf8("BACKWARD"))).awaitValue(awaitTime)

          val schemaId1 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(fullCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          schemaId1.id should equal(1L)

          val schemaId2 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(backwardCompatibleSchemaDefinition))
          ).awaitValueUnsafe(awaitTime).get
          schemaId2.id should equal(2L)

          val tryOutput = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(forwardCompatibleSchemaDefinition))
          ).awaitOutputUnsafe(awaitTime).get
          log.info(s"a response for compatibility violating schema against the restriction is $tryOutput")

          tryOutput.status.code should equal (409)

          val versions = TestSchemaService.readVersions(
            Input.get(s"/subjects/$schemaSubject/versions")
          ).awaitValueUnsafe(awaitTime).get
          versions should equal (Seq("v1.0.0", "v1.1.0"))
        }


        // Abnormal: try to read a schema with not existing or invalid id
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          val tryToNotExisting = TestSchemaService.readById(
            Input.get(s"/schemas/ids/5")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"trial to read not existing id is $tryToNotExisting")
          tryToNotExisting.status.code should equal (404)


          val tryToNegativeId = TestSchemaService.readById(
            Input.get(s"/schemas/ids/-2")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"trial to read a negative id of schema is $tryToNegativeId")
          tryToNegativeId.status.code should equal(422)
        }


        // Abnormal: try to read a schema with not existing subject or version
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValue(awaitTime)

          // try to read under a not existing subject
          val tryToNotExistingSubject = TestSchemaService.readByVersion(
            Input.get(s"/subjects/hoge/versions/v1.0.0")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"trial to read a schema under a not existing subject is $tryToNotExistingSubject")
          tryToNotExistingSubject.status.code should equal (404)


          // try to read a not existing version of schema under an existing subject
          val tryToNotExistingVersion = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/v1.0.1")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"trial to read a not existing version of schema is $tryToNotExistingVersion")
          tryToNotExistingVersion.status.code should equal (404)


          val tryInvalidFormat = TestSchemaService.readByVersion(
            Input.get(s"/subjects/$schemaSubject/versions/1")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"trial to read a schema by with invalidly formatted schema")
          tryInvalidFormat.status.code should equal (422)
        }


        // Abnormal: read existing versions under the not existing subject
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          val versions = TestSchemaService.readVersions(
            Input.get(s"/subjects/$schemaSubject/versions")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"version under the not existing subject is $versions")
          versions shouldBe empty
        }


        // Abnormal: try to create a schema with an invalid definition
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          val tryOutput = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions").withBody[Application.Json](Buf.Utf8(invalidDefinition))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"response for a request to create an invalid schema is $tryOutput")
          tryOutput.status.code should equal (422)


          // post without a definition in body
          val tryOutput2 = TestSchemaService.create(
            Input.post(s"/subjects/$schemaSubject/versions")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"response for a request with empty body is $tryOutput2")
          tryOutput2.status.code should equal (400)
        }


        // Abnormal: check compatibility of an invalid schema
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create(Input.post(s"/subjects/$schemaSubject").withBody[Text.Plain](Buf.Utf8(subjectDescription))).awaitValue(awaitTime)

          TestSchemaService.create(
            Input.post(s"/subjects/$subjectName/versions").withBody[Application.Json](Buf.Utf8(schemaDefinition))
          ).awaitValueUnsafe(awaitTime).get

          val invalidIsCompatible = TestSchemaService.checkCompatibility(
            Input.post(s"/compatibility/subjects/$subjectName/versions/latest").withBody[Application.Json](Buf.Utf8(invalidDefinition))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"'invalid is compatible' is $invalidIsCompatible")
          invalidIsCompatible.status.code should equal (422)


          val emptyIsCompatible = TestSchemaService.checkCompatibility(
            Input.post(s"/compatibility/subjects/$subjectName/versions/latest")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"'empty is compatible' is $emptyIsCompatible")
          emptyIsCompatible.status.code should equal (400)
        }

      }
    }
  }

}
