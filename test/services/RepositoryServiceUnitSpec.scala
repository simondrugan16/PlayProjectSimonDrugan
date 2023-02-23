package services

import baseSpec.{BaseSpec, BaseSpecWithApplication}
import com.mongodb.client.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.bson._
import controllers.ApplicationController
import models.{APIError, Book, DataModel}
import org.mongodb.scala.bson
import org.mongodb.scala.result.DeleteResult
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Futures.whenReady
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import repositories.{DataRepository, DataRepositoryTrait}

import scala.concurrent.{Await, ExecutionContext, Future}
import play.api.http.{HttpEntity, Status}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Results.Accepted

class RepositoryServiceUnitSpec extends BaseSpecWithApplication with MockFactory {

  val mockRepositoryTrait: DataRepositoryTrait = mock[DataRepositoryTrait]

  val testRepositoryService = new RepositoryService(mockRepositoryTrait)

  val myBsonValue: BsonValue = new BsonString("PR001")
  val updateResultTest: UpdateResult = UpdateResult.acknowledged(1, 1, myBsonValue)

  val myResponseHeader: ResponseHeader = ResponseHeader(500)
  val customInternalServerError: Result = Result(myResponseHeader, HttpEntity.NoEntity)

  val deleteResultTest: DeleteResult = DeleteResult.acknowledged(1)

  private val book: Book = Book(
    "abcd",
    "THE WINDS OF WINTER",
    "test description",
    "27/08/1990"
  )

  val gameOfThrones: JsValue = Json.obj(
    "id" -> "someId",
    "volumeInfo" -> "A Game of Thrones",
    "publisher" -> "The best book!!!",
    "publishedDate" -> "LAST WEEK"
  )

  val nameOfTheWind: DataModel = DataModel(
    "PR001",
    "The Name of the Wind",
    "Tor Books",
    2007
  )

  val updatedNameOfTheWind: DataModel = DataModel(
    "PR001",
    "The Name of the Wind",
    "Gollancz",
    2007
  )

  val badDataModel: DataModel = DataModel(
    "",
    "",
    "",
    0
  )

  val indexedBooks: Seq[DataModel] = Seq(nameOfTheWind)

  val whatIsIt: JsValue = Json.toJson(gameOfThrones)

  "RepositoryService .index" should {
    "return a seq of books" in {

      (() => mockRepositoryTrait.index())
        .expects()
        .returning(Future(Right(indexedBooks)))

      whenReady(testRepositoryService.index()) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe indexedBooks
      }
    }

    "return an API error when books cannot be found" in {

      (() => mockRepositoryTrait.index())
        .expects()
        .returning(Future(Left(APIError.BadAPIResponse(1, "Books could not be found"))))

      whenReady(testRepositoryService.index()) {
        case Left(value) => value shouldBe APIError.BadAPIResponse(1, "Books could not be found")
        case Right(value) => value shouldBe fail("test should not have failed, returning a right rather than the left which was expected")
      }
    }
  }

  "RepositoryService .create" should {

    "create a book in the database" in {
      (mockRepositoryTrait.create(_: DataModel))
        .expects(nameOfTheWind)
        .returning(Future(Right(nameOfTheWind)))

      whenReady(testRepositoryService.create(nameOfTheWind)) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe nameOfTheWind
      }
    }

    "return an internal server error when books cannot be created" in {

      (mockRepositoryTrait.create(_: DataModel))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

        whenReady(testRepositoryService.create(badDataModel)) {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => value shouldBe fail("test should not have failed, having returned a right rather than the left which was expected")
      }
    }
  }

  "RepositoryService .read" should {

    "read a book in the database" in {
      (mockRepositoryTrait.read(_: String))
        .expects("PR001")
        .returning(Future(Right(nameOfTheWind)))

      whenReady(testRepositoryService.read("PR001")) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe nameOfTheWind
      }
    }

    "return an internal server error when a book cannot be read" in {

      (mockRepositoryTrait.read(_: String))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      whenReady(testRepositoryService.read("PR001")) {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => value shouldBe fail("test should not have failed, having returned a right rather than the left which was expected")
      }
    }
  }

  "RepositoryService .readByTitle" should {

    "read a book in the database" in {
      (mockRepositoryTrait.readByTitle(_: String))
        .expects("The Name of the Wind")
        .returning(Future(Right(nameOfTheWind)))

      whenReady(testRepositoryService.readByTitle("The Name of the Wind")) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe nameOfTheWind
      }
    }

    "return an internal server error when a book cannot be read by title" in {

      (mockRepositoryTrait.readByTitle(_: String))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      whenReady(testRepositoryService.readByTitle("The Name of the Wind")) {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => value shouldBe fail("test should not have failed, having returned a right rather than the left which was expected")
      }
    }
  }

  "RepositoryService .update" should {

    "read a book in the database" in {
      (mockRepositoryTrait.update(_: String, _: DataModel))
        .expects(*, *)
        .returning(Future(Right(updateResultTest)))

      whenReady(testRepositoryService.update("PR001", nameOfTheWind)) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe updateResultTest
      }
    }

    "return an internal server error when books cannot be updated" in {

      (mockRepositoryTrait.update(_: String, _: DataModel))
        .expects(*, *)
        .returning(Future(Left(customInternalServerError)))

      whenReady(testRepositoryService.update("", badDataModel)) {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => value shouldBe fail("test should not have failed, having returned a right rather than the left which was expected")
      }
    }

  }

  "RepositoryService .updateByFieldAndUpdate" should {

    "read a book in the database" in {
      (mockRepositoryTrait.updateByFieldAndUpdate(_: String, _: String, _: String))
        .expects(*, *, *)
        .returning(Future(Right(updateResultTest)))

      whenReady(testRepositoryService.updateByFieldAndUpdate("PR001", "description", "ash and bone")) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe updateResultTest
      }
    }

    "return an internal server error when books cannot be updated by the field and the update to change the DataModel" in {

      (mockRepositoryTrait.updateByFieldAndUpdate(_: String, _: String, _: String))
        .expects(*, *, *)
        .returning(Future(Left(customInternalServerError)))

      whenReady(testRepositoryService.updateByFieldAndUpdate("PR001", "DASKRIPTION", "Gollancz")) {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => value shouldBe fail("test should not have failed, having returned a right rather than the left which was expected")
      }
    }
  }

  "RepositoryService .delete" should {

    "read a book in the database" in {
      (mockRepositoryTrait.delete(_: String))
        .expects("PR001")
        .returning(Future(Right(deleteResultTest)))

      whenReady(testRepositoryService.delete("PR001")) {
        case Left(value) => value shouldBe fail("shouldn't have failed")
        case Right(value) => value shouldBe deleteResultTest
      }
    }

    "return an internal server error when books cannot be updated by the field and the update to change the DataModel" in {

      (mockRepositoryTrait.delete(_: String))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      whenReady(testRepositoryService.delete("Non existent DataModel ID")) {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => value shouldBe fail("test should not have failed, having returned a right rather than the left which was expected")
      }
    }
  }
}
