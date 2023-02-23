package controllers

import baseSpec.BaseSpecWithApplication
import models.DataModel
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication {

  val TestApplicationController = new ApplicationController(
    component,
    executionContext,
    service,
    repositoryService
  )

  private val dataModel: DataModel = DataModel(
    "abcd",
    "THE WINDS OF WINTER",
    "test description",
    100
  )

  private val dataModelUpdatedField: DataModel = DataModel(
    "abcd",
    "NOT COMING OUT",
    "test description",
    100
  )

  private val dataModelUpdated: DataModel = DataModel(
    "X Æ A-Xii",
    "test upDATED",
    "good desc",
    419
  )

  override def beforeEach(): Unit = await(repository.deleteAll())

  "ApplicationController .index()" should {

    val result = TestApplicationController.index()(FakeRequest())

    "return a seq of books" in {
      beforeEach()
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create" should {
    "create a book in the database" in {
      beforeEach()

      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(request)

      status(createResult) shouldBe Status.CREATED

    }

    "throw a BadRequest when given a non DataModel object" in {
      beforeEach()

      val badRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson("noot"))
      val createResult: Future[Result] = TestApplicationController.create()(badRequest)

      status(createResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .read" should {
    "read a book in the database by id" in {
      beforeEach()

      val request: FakeRequest[JsValue] = buildPost(s"/api").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(request)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.read("abcd")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModel

    }

    "throw an bad request when given an empty string" in {
      beforeEach()

      val request: FakeRequest[AnyContent] = buildGet("/api/")
      val readResult: Future[Result] = TestApplicationController.read("")(request)

      status(readResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .readByTitle" should {
    "read a book in the database by title" in {
      beforeEach()

      val request: FakeRequest[JsValue] = buildPost(s"/api").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(request)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.readByTitle("THE WINDS OF WINTER")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModel

    }

    "throw an bad request when given an empty string title" in {
      beforeEach()

      val request: FakeRequest[AnyContent] = buildGet("/api")

      val readResult: Future[Result] = TestApplicationController.read("")(request)

      status(readResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .update(id: String)" should {
    "update a book in the database by id and data model" in {
      beforeEach()

      val createRequest: FakeRequest[JsValue] = buildGet("/api").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModelUpdated))
      val updateResult: Future[Result] = TestApplicationController.update(dataModelUpdated._id)(updateRequest)

      val readResult: Future[Result] = TestApplicationController.read("X Æ A-Xii")(FakeRequest())

      status(updateResult) shouldBe Status.ACCEPTED
      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModelUpdated

    }

    "throw an BadRequest when given a body with the incorrect format" in {
      beforeEach()

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${dataModel._id}").withBody[JsValue](Json.toJson("noot"))
      val updateResult: Future[Result] = TestApplicationController.update(dataModel._id)(updateRequest)

      status(updateResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .updateByFieldAndUpdate(id: String, field: String, update: String)" should {
    "update a book in the database by id, a field to update, and the update itself" in {
      beforeEach()

      val createRequest: FakeRequest[JsValue] = buildGet("/api").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${dataModel._id}").withBody[JsValue](Json.toJson(s"${dataModel._id}", "name", "NOT COMING OUT"))
      val updateResult: Future[Result] = TestApplicationController.updateByFieldAndUpdate(dataModel._id, "name", "NOT COMING OUT")(updateRequest)

      status(updateResult) shouldBe Status.ACCEPTED

      val readResult: Future[Result] = TestApplicationController.read(s"${dataModel._id}")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModelUpdatedField

    }

    "throw an BadRequest when given an empty id, field, or update" in {
      beforeEach()

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${dataModel._id}").withBody[JsValue](Json.toJson("noot"))
      val updateResult: Future[Result] = TestApplicationController.update(dataModel._id)(updateRequest)

      status(updateResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .delete(id: String)" should {

    "delete a book in the database given its id" in {
      beforeEach()

      val createRequest: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val deleteResult: Future[Result] = TestApplicationController.delete("abcd")(FakeRequest())

      status(deleteResult) shouldBe Status.ACCEPTED

    }

    "throw an BadRequest when .delete is given an empty Book ID string" in {
      beforeEach()

      val request: FakeRequest[AnyContent] = buildDelete("/api/")

      val readResult: Future[Result] = TestApplicationController.delete("")(request)

      status(readResult) shouldBe Status.BAD_REQUEST

    }
  }

  "ApplicationController .addBook()" should {

    "use a form to load my AddANewBook.scala.html page" in {
      beforeEach()

      val createRequest: FakeRequest[AnyContent] = buildGet("/addanewperson/form")
      val createResult: Future[Result] = TestApplicationController.addBook()(createRequest)

      status(createResult) shouldBe Status.OK

    }
  }

  "ApplicationController .addBookForm()SD" should {

    "use a form to create a book (DataModel)" in {
      beforeEach()

      val addBookFormRequest = buildPost("/addanewperson/form")
        .withFormUrlEncodedBody(
          "_id" -> "112",
          "name" -> "bla",
          "description" -> "abl",
          "numSales" -> "33").withCSRFToken
      val addBookFormResult = TestApplicationController.addBookForm()(addBookFormRequest)

      status(addBookFormResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.read("112")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe DataModel("112", "bla", "abl", 33)

    }

    "throw a BadRequest when .addBookForm is given an invalid completed form" in {
      beforeEach()

      val addBookFormRequest = buildPost("/addanewperson/form")
        .withFormUrlEncodedBody("Bad Form" -> "Bad Form").withCSRFToken
      val addBookFormResult = TestApplicationController.addBookForm()(addBookFormRequest)

      status(addBookFormResult) shouldBe Status.BAD_REQUEST

    }
  }
}
