package jp.co.cyberagent.typebook

import java.util.concurrent.TimeUnit

import scala.language.postfixOps

import com.twitter.io.Buf
import com.twitter.util.Duration
import io.finch.{Input, Text}
import org.scalatest._

import jp.co.cyberagent.typebook.api.SubjectServiceTrait
import jp.co.cyberagent.typebook.model.Subject


object SubjectServiceIntegrationSpec {
  val subjectName = "test-subject"
  val subjectDescription = "This is a test subject."
  val subjectEmptyName = "test-empty-subject"
  val subjectEmptyDescription = ""
}

class SubjectServiceIntegrationSpec extends FlatSpec with StorageBackend with StorageClientUtil with Matchers {
  
  import SubjectServiceIntegrationSpec._

  var buffer = Array[Byte]()
  val awaitTime = Duration(10, TimeUnit.SECONDS)

  // FIXME It might be better to use WordSpec but there is no way to call dockerComposition out of "in" clause.
  // Another way is call it in each "in" clause but it makes test too slow...
  // For those reasons, gave up to use WordSpec and adopt FlatSpec for IntegrationTest

  "A SubjectService" should "correctly serve CRUD requests" taggedAs IntegrationTest in {

    withRule(dockerComposition("subject-service")) { dc =>
      withDbClient(dc) { client =>

        import io.circe.generic.auto._
        import io.finch.circe._

        // Service API to test
        object TestSubjectService extends TestMySqlBackend(client) with SubjectServiceTrait


        // Normal: Create and read a single record
        withRule(UsingCleanTables(client)) { _ =>
          // create a subject
          val id = TestSubjectService.create(
            Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"id is $id")
          id should equal (0)


          // read a registered subject
          val subject = TestSubjectService.read(
            Input.get(s"/subjects/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"subject is $subject")
          subject.name should equal (subjectName)
          subject.description.isDefined shouldBe true
          subject.description.get should equal (subjectDescription)


          // read a specific field
          val nameBuf = TestSubjectService.readField(
            Input.get(s"/subjects/$subjectName", ("field", "name"))
          ).awaitValueUnsafe(awaitTime).get
          buffer = new Array[Byte](nameBuf.length)
          nameBuf.write(buffer, 0)
          val name = new String(buffer)
          log.info(s"name is $name")
          name should equal (subjectName)


          val descriptionBuf = TestSubjectService.readField(
            Input.get(s"/subjects/$subjectName", ("field", "description"))
          ).awaitValueUnsafe(awaitTime).get
          buffer = new Array[Byte](descriptionBuf.length)
          descriptionBuf.write(buffer, 0)
          val description = new String(buffer)
          log.info(s"description is $description")
          description should equal (subjectDescription)
        }



        // Normal: Create and read multiple records
        withRule(UsingCleanTables(client)) { _ =>
          // create 2 subjects
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)
          TestSubjectService.create( Input.post(s"/subjects/$subjectEmptyName").withBody[Text.Plain](Buf.Utf8(subjectEmptyDescription)) ).awaitValue(awaitTime)

          // read all subjects
          val subjects = TestSubjectService.readAll(
            Input.get("/subjects")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"subjects are $subjects")
          subjects should contain theSameElementsAs Seq(subjectName,subjectEmptyName)

        }



        // Normal: Create a subject with empty description and update it to subjectDescription
        withRule(UsingCleanTables(client)) { _ =>
          TestSubjectService.create( Input.post(s"/subjects/$subjectEmptyName").withBody[Text.Plain](Buf.Utf8(subjectEmptyDescription)) ).awaitValue(awaitTime)

          val updatedRows = TestSubjectService.update(
            Input.put(s"/subjects/$subjectEmptyName").withBody[Text.Plain](Buf.Utf8(subjectDescription))
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"the number of updated rows is $updatedRows")
          updatedRows should equal (1L)


          val updated = TestSubjectService.read(
            Input.get(s"/subjects/$subjectEmptyName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"after updated subject became $updated")
          updated should equal (Subject(subjectEmptyName, Some(subjectDescription)))
        }



        // Normal: Create and Delete a subject
        withRule(UsingCleanTables(client)) { _ =>
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          val deletedRows = TestSubjectService.del(
            Input.delete(s"/subjects/$subjectName")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"the number of deleted rows is $deletedRows")
          deletedRows should equal (1L)

          val deleted = TestSubjectService.readAll(
            Input.get(s"/subjects")
          ).awaitValueUnsafe(awaitTime).get
          log.info(s"after deleted subjects are $deleted")
          deleted shouldBe empty
        }



        // Abnormal: try to read not existing subject
        // it should return 404 NotFound
        withRule(UsingCleanTables(client)) { _ =>

          val tryOutput = TestSubjectService.read(
            Input.get("/subjects/hoge")
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for not existing subject is $tryOutput")
          tryOutput.status.code should equal (404)
        }


        // Abnormal: try to read a not existing field of existing subjects
        // The endpoint read should return 400 BadRequest
        withRule(UsingCleanTables(client)) { _ =>
          TestSubjectService.create( Input.post(s"/subjects/$subjectName").withBody[Text.Plain](Buf.Utf8(subjectDescription)) ).awaitValue(awaitTime)

          val tryOutput = TestSubjectService.readField(
            Input.get(s"/subjects/$subjectName", ("field", "foo"))
          ).awaitOutputUnsafe(awaitTime).get

          log.info(s"A response for not existing field is $tryOutput")
          tryOutput.status.code should equal (400)

        }

      }
    }
  }

}
