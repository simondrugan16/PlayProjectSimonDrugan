package services

import baseSpec.BaseSpecWithApplication
import com.mongodb.client.result.{DeleteResult, UpdateResult}
import controllers.ApplicationController
import models.{APIError, DataModel}
import org.mongodb.scala.bson.{BsonString, BsonValue}
import org.scalamock.scalatest.MockFactory
import play.api.http.{HttpEntity, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}

import scala.concurrent.Future

class ApplicationControllerUnitSpec extends BaseSpecWithApplication with MockFactory {

  val mockRepositoryService: RepositoryService = mock[RepositoryService]

  val TestApplicationController = new ApplicationController(
    component,
    executionContext,
    service,
    mockRepositoryService
  )

  val myResponseHeader: ResponseHeader = ResponseHeader(500)
  val customInternalServerError: Result = Result(myResponseHeader, HttpEntity.NoEntity)

  val myBsonValue: BsonValue = new BsonString("PR001")
  val updateResultTest: UpdateResult = UpdateResult.acknowledged(1, 1, myBsonValue)

  val deleteResultTest: DeleteResult = DeleteResult.acknowledged(1)

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

  val indexMockReturn: Seq[DataModel] = Seq(nameOfTheWind)

  "ApplicationController .index()" should {


    "return a seq of books" in {
      (() =>mockRepositoryService.index())
        .expects()
        .returning(Future(Right(indexMockReturn)))

      val indexRequest = buildGet("/api")
      val indexResult = TestApplicationController.index()(indexRequest)

      status(indexResult) shouldBe Status.OK
      contentAsJson(indexResult).as[Seq[DataModel]] shouldBe indexMockReturn
    }

    "return an error when a sequence of books cannot be returned" in {
      (() => mockRepositoryService.index())
        .expects()
        .returning(Future(Left(APIError.BadAPIResponse(500, "Internal Server Error Occurred"))))

      val indexRequest = buildGet("/api")
      val indexResult = TestApplicationController.index()(indexRequest)

      status(indexResult) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "ApplicationController .create" should {

    "create a book in the database" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED
    }

    "return an internal server error when books cannot be created" in {

      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      val badRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(badDataModel))
      val createResult: Future[Result] = TestApplicationController.create()(badRequest)

      status(createResult) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "ApplicationController .read" should {
    "read a book in the database by id" in {

      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.read(_: String))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.read("PR001")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe nameOfTheWind
    }

    "throw an bad request when given an empty string" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.read(_: String))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.read("INCORRECT_DATAMODEL_ID")(FakeRequest())

      status(readResult) shouldBe Status.INTERNAL_SERVER_ERROR

    }
  }

  "ApplicationController .readByTitle" should {
    "read a book in the database by title" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.readByTitle(_: String))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.readByTitle("The Name of the Wind")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe nameOfTheWind

    }

    "throw an bad request when given an empty string title" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.readByTitle(_: String))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.readByTitle("INCORRECT_DATAMODEL_TITLE")(FakeRequest())

      status(readResult) shouldBe Status.INTERNAL_SERVER_ERROR

    }
  }

  "ApplicationController .update(id: String)" should {
    "update a book in the database by id and data model" in {

      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.update(_: String, _: DataModel))
        .expects(*, *)
        .returning(Future(Right(updateResultTest)))

      (mockRepositoryService.read(_: String))
        .expects(*)
        .returning(Future(Right(updatedNameOfTheWind)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${nameOfTheWind._id}").withBody[JsValue](Json.toJson(updatedNameOfTheWind))
      val updateResult: Future[Result] = TestApplicationController.update(nameOfTheWind._id)(updateRequest)

      status(updateResult) shouldBe Status.ACCEPTED

      val readResult: Future[Result] = TestApplicationController.read("PR001")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe updatedNameOfTheWind

    }

    "throw an internal server error when given a body with the incorrect format" in {

      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.update(_: String, _: DataModel))
        .expects(*, *)
        .returning(Future(Left(customInternalServerError)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${nameOfTheWind._id}").withBody[JsValue](Json.toJson(badDataModel))
      val updateResult: Future[Result] = TestApplicationController.update(nameOfTheWind._id)(updateRequest)

      status(updateResult) shouldBe Status.INTERNAL_SERVER_ERROR

    }
  }

  "ApplicationController .updateByFieldAndUpdate(id: String, field: String, update: String)" should {
    "update a book in the database by id, a field to update, and the update itself" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.updateByFieldAndUpdate(_: String, _: String, _: String))
        .expects(*, *, *)
        .returning(Future(Right(updateResultTest)))

      (mockRepositoryService.read(_: String))
        .expects(*)
        .returning(Future(Right(updatedNameOfTheWind)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${nameOfTheWind._id}").withBody[JsValue](Json.toJson(s"${nameOfTheWind._id}", "description", "Gollancz"))
      val updateResult: Future[Result] = TestApplicationController.updateByFieldAndUpdate(nameOfTheWind._id, "description", "Gollancz")(updateRequest)

      status(updateResult) shouldBe Status.ACCEPTED

      val readResult: Future[Result] = TestApplicationController.read("PR001")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe updatedNameOfTheWind

    }

    "throw an BadRequest when given an empty id, field, or update" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.updateByFieldAndUpdate(_: String, _: String, _: String))
        .expects(*, *, *)
        .returning(Future(Left(customInternalServerError)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${nameOfTheWind._id}").withBody[JsValue](Json.toJson(s"${nameOfTheWind._id}", "description", "Gollancz"))
      val updateResult: Future[Result] = TestApplicationController.updateByFieldAndUpdate("", "", "")(updateRequest)

      status(updateResult) shouldBe Status.INTERNAL_SERVER_ERROR

    }
  }

  "ApplicationController .delete(id: String)" should {

    "delete a book in the database given its id" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.delete(_: String))
        .expects(*)
        .returning(Future(Right(deleteResultTest)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val deleteRequest: FakeRequest[AnyContent] = buildDelete(s"/api/${nameOfTheWind._id}")
      val deleteResult: Future[Result] = TestApplicationController.delete(nameOfTheWind._id)(deleteRequest)

      status(deleteResult) shouldBe Status.ACCEPTED

    }

    "throw an BadRequest when .delete is given an empty Book ID string" in {
      (mockRepositoryService.create(_: DataModel))
        .expects(*)
        .returning(Future(Right(nameOfTheWind)))

      (mockRepositoryService.delete(_: String))
        .expects(*)
        .returning(Future(Left(customInternalServerError)))

      val createRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(nameOfTheWind))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val deleteRequest: FakeRequest[AnyContent] = buildDelete(s"/api/INVALID_DATAMODEL_ID")
      val deleteResult: Future[Result] = TestApplicationController.delete("INVALID_DATAMODEL_ID")(deleteRequest)

      status(deleteResult) shouldBe Status.INTERNAL_SERVER_ERROR

    }
  }
}
