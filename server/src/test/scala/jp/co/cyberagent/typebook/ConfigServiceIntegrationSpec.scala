package jp.co.cyberagent.typebook

import java.util.concurrent.TimeUnit

import scala.language.postfixOps

import com.twitter.io.Buf
import com.twitter.util._
import io.finch.{Application, Input, Text}
import org.scalatest._

import jp.co.cyberagent.typebook.api.{ConfigServiceTrait, SubjectServiceTrait}
import jp.co.cyberagent.typebook.compatibility.SchemaCompatibility._
import jp.co.cyberagent.typebook.model.RegistryConfig


class ConfigServiceIntegrationSpec extends FlatSpec with StorageBackend with StorageClientUtil with Matchers {

  import SubjectServiceIntegrationSpec._

  var buffer = Array[Byte]()
  val awaitTime = Duration(10, TimeUnit.SECONDS)

  "A ConfigService" should "correctly service CRUD requests" taggedAs IntegrationTest in {
    withRule(dockerComposition("config-service")) { dc =>
      withDbClient(dc) { client =>


        import io.circe.generic.auto._
        import io.finch.circe._

        // Service API to test
        object TestConfigService extends TestMySqlBackend(client) with ConfigServiceTrait

        // for subject registration
        object TestSubjectService extends TestMySqlBackend(client) with SubjectServiceTrait


        // Normal: set and read a valid compatibility config
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          // set a compatibility restriction to FULL
          val updatedRows1 = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
              """
                |{
                |   "compatibility": "FULL"
                |}
              """.stripMargin))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows1: $updatedRows1")
          updatedRows1 should equal(1L)


          // read a current registry config
          val registryConf1 = TestConfigService.read(
            Input.get(s"/config/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"RegistryConfig1: $registryConf1")
          registryConf1.compatibility should equal(FullCompatible)



          // update a compatibility to BACKWARD
          val updatedRows2 = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "compatibility": "BACKWARD"
              |}
            """.stripMargin))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows2: $updatedRows2")
          updatedRows2 should equal(1L)


          val registryConf2 = TestConfigService.read(
            Input.get(s"/config/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"RegistryConfig2: $registryConf2")
          registryConf2.compatibility should equal(BackwardCompatible)



          // update a compatibility to FORWARD
          val updatedRows3 = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "compatibility": "FORWARD"
              |}
            """.stripMargin))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows3: $updatedRows3")
          updatedRows3 should equal(1L)


          val registryConf3 = TestConfigService.read(
            Input.get(s"/config/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"registryConf3: $registryConf3")
          registryConf3.compatibility should equal(ForwardCompatible)



          // update a compatibility to FORWARD
          val updatedRows4 = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "compatibility": "NONE"
              |}
            """.stripMargin))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows4: $updatedRows4")
          updatedRows4 should equal(1L)


          val registryConf4 = TestConfigService.read(
            Input.get(s"/config/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"registryConf4: $registryConf4")
          registryConf4.compatibility should equal(NotCompatible)
        }



        // Normal: set and read a compatibility with a valid value
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          // set a compatibility restriction to FULL
          val updatedRows1 = TestConfigService.setProperty(
            Input.put(s"/config/$subjectName/properties/compatibility").withBody[Text.Plain](Buf.Utf8("FULL"))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows1: $updatedRows1")
          updatedRows1 should equal(1L)


          // read a current compatibility value
          val registryCompatibilityBuf1 = TestConfigService.readProperty(
            Input.get(s"/config/$subjectName/properties/compatibility")
          ).awaitValueUnsafe(awaitTime).get

          buffer = new Array[Byte](registryCompatibilityBuf1.length)
          registryCompatibilityBuf1.write(buffer, 0)
          val registryCompatibility1 = new String(buffer)
          log.info(s"RegistryCompatibility1: $registryCompatibility1")
          registryCompatibility1 should equal("FULL")


          // set a compatibility restriction to BACKWARD
          val updatedRows2 = TestConfigService.setProperty(
            Input.put(s"/config/$subjectName/properties/compatibility").withBody[Text.Plain](Buf.Utf8("BACKWARD"))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows2: $updatedRows2")
          updatedRows2 should equal(1L)


          // read a current compatibility value
          val registryCompatibilityBuf2 = TestConfigService.readProperty(
            Input.get(s"/config/$subjectName/properties/compatibility")
          ).awaitValueUnsafe(awaitTime).get

          buffer = new Array[Byte](registryCompatibilityBuf2.length)
          registryCompatibilityBuf2.write(buffer, 0)
          val registryCompatibility2 = new String(buffer)
          log.info(s"RegistryCompatibility2: $registryCompatibility2")
          registryCompatibility2 should equal("BACKWARD")


          // set a compatibility restriction to FORWARD
          val updatedRows3 = TestConfigService.setProperty(
            Input.put(s"/config/$subjectName/properties/compatibility").withBody[Text.Plain](Buf.Utf8("FORWARD"))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows3: $updatedRows3")
          updatedRows3 should equal(1L)


          // read a current compatibility value
          val registryCompatibilityBuf3 = TestConfigService.readProperty(
            Input.get(s"/config/$subjectName/properties/compatibility")
          ).awaitValueUnsafe(awaitTime).get

          buffer = new Array[Byte](registryCompatibilityBuf3.length)
          registryCompatibilityBuf3.write(buffer, 0)
          val registryCompatibility3 = new String(buffer)
          log.info(s"RegistryCompatibility3: $registryCompatibility3")
          registryCompatibility3 should equal("FORWARD")


          // set a compatibility restriction to BACKWARD
          val updatedRows4 = TestConfigService.setProperty(
            Input.put(s"/config/$subjectName/properties/compatibility").withBody[Text.Plain](Buf.Utf8("NONE"))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"updatedRows4: $updatedRows4")
          updatedRows4 should equal(1L)

          // read a current compatibility value
          val registryCompatibilityBuf4 = TestConfigService.readProperty(
            Input.get(s"/config/$subjectName/properties/compatibility")
          ).awaitValueUnsafe(awaitTime).get

          buffer = new Array[Byte](registryCompatibilityBuf4.length)
          registryCompatibilityBuf4.write(buffer, 0)
          val registryCompatibility4 = new String(buffer)
          log.info(s"RegistryCompatibility4: $registryCompatibility4")
          registryCompatibility4 should equal("NONE")

        }



        // Normal: Create and Delete a valid compatibility config
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          // set a compatibility restriction to FULL
          TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "compatibility": "FULL"
              |}
            """.stripMargin))
          ).awaitValue(awaitTime)

          val deletedRows1 = TestConfigService.del(
            Input.delete(s"/config/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"deleteRows1: $deletedRows1")
          deletedRows1 should equal(1L)


          // set a compatibility restriction to FULL
          TestConfigService.setProperty(
            Input.put(s"/config/$subjectName/properties/compatibility").withBody[Text.Plain](Buf.Utf8("FULL"))
          ).awaitValueUnsafe(awaitTime).get

          val deletedRows2 = TestConfigService.delProperty(
            Input.delete(s"/config/$subjectName/properties/compatibility")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"deleteRows2: $deletedRows2")
          deletedRows2 should equal(1L)
        }

        // Normal: try to read config values that have not been set, it should return default values
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          val actual = TestConfigService.read(
            Input.get(s"/config/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"default config for subject $subjectName is $actual")
          actual should equal (RegistryConfig.default)

          val actualPropBuf = TestConfigService.readProperty(
            Input.get(s"/config/$subjectName/properties/${RegistryConfig.Compatibility}")
          ).awaitValueUnsafe(awaitTime).get
          buffer = new Array[Byte](actualPropBuf.length)
          actualPropBuf.write(buffer, 0)
          val actualProp = new String(buffer)
          log.info(s"default compatibility value for subject $subjectName is $actualProp")
          actualProp should equal (RegistryConfig.default.compatibility.toString)
        }

        // Abnormal: try to read a config for non existent subject, it should return 404
        withRule(UsingCleanTables(client)) { _ =>
          val actual = TestConfigService.read(
            Input.get(s"/config/non-existent-subject")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for non existent subject is $actual")
          actual.status.code should equal (404)
        }

        // Abnormal: try to create an invalid compatibility configuration and it should return 422
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          // set a compatibility restriction with an invalid value "foo"
          val tryOutput1 = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "compatibility": "foo"
              |}
            """.stripMargin))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for an invalid compatibility is $tryOutput1")
          tryOutput1.status.code should equal (422)



          // set a compatibility restriction with an invalid value "foo" and it should return 400
          val tryOutput2 = TestConfigService.setProperty(
            Input.put(s"/config/$subjectName/properties/compatibility").withBody[Text.Plain](Buf.Utf8("foo"))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for an invalid compatibility is $tryOutput2")
          tryOutput2.status.code should equal (422)
        }



        // Abnormal: Putting an invalid json and it should return 422
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          // put an invalid json
          val tryOutput = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "compatibility: "foo",
              |}
            """.stripMargin))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for an invalid json is $tryOutput")
          tryOutput.status.code should equal (422)
        }


        // Abnormal: Putting a config including an invalid property
        withRule(UsingCleanTables(client)) { _ =>

          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValueUnsafe().get

          // put an invalid json
          val updatedRows = TestConfigService.set( Input.put(s"/config/$subjectName").withBody[Application.Json](Buf.Utf8(
            """
              |{
              |   "foo": "bar",
              |   "compatibility": "FULL"
              |}
            """.stripMargin))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"UpdatedRows for the request that includes an invalid property is $updatedRows")
          updatedRows should equal(1L)
        }


        // Abnormal: try to read a not existing property
        withRule(UsingCleanTables(client)) { _ =>
          // create a test subject for setting configs
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          // read a not existing property "foo"
          val tryOutput = TestConfigService.readProperty( Input.get(s"/config/$subjectName/properties/foo" )).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for a not existing property is $tryOutput")
          tryOutput.status.code should equal (422)
        }

      }
    }
  }

}
