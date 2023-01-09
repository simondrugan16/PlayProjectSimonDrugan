package controllers

import baseSpec.BaseSpecWithApplication
import com.fasterxml.jackson.annotation.ObjectIdGenerators.None
import models.DataModel
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}

import scala.concurrent.Future
import scala.util.Failure

class ApplicationControllerSpec extends BaseSpecWithApplication {

  val TestApplicationController = new ApplicationController(
    component)(
    repository
  )

  private val dataModel: DataModel = DataModel(
    "abcd",
    "test name",
    "test description",
    100
  )

  private val dataModelUpdated: DataModel = DataModel(
    "X Æ A-Xii",
    "test upDATED",
    "good desc",
    419
  )

  override def beforeEach(): Unit = repository.deleteAll()

  override def afterEach(): Unit = repository.deleteAll()

  beforeEach()
  afterEach()

  "ApplicationController .index()" should {
    val result = TestApplicationController.index()(FakeRequest())

    "return TODO" in {
      status(result) shouldBe Status.OK
    }
  }

  "ApplicationController .create" should {

    "create a book in the database" in {

      beforeEach()

      val request: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(request)

      status(createResult) shouldBe Status.CREATED

      afterEach()
    }

    "throw a BadRequest when given a non DataModel object" in {

      beforeEach()

      val badRequest: FakeRequest[JsValue] = buildPost("/api").withBody[JsValue](Json.toJson("noot"))
      val createResult: Future[Result] = TestApplicationController.create()(badRequest)

      status(createResult) shouldBe Status.BAD_REQUEST

      afterEach()

    }
  }

  "ApplicationController .read" should {

    "read a book in the database by id" in {

      beforeEach()

      val request: FakeRequest[JsValue] = buildPost(s"/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(request)

      status(createResult) shouldBe Status.CREATED

      val readResult: Future[Result] = TestApplicationController.read("abcd")(FakeRequest())

      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModel

      afterEach()

    }

    "throw an bad request when given an empty stringSD" in {
      beforeEach()

      val request: FakeRequest[AnyContent] = buildGet("/api/")

      val readResult: Future[Result] = TestApplicationController.read("")(request)

      status(readResult) shouldBe Status.BAD_REQUEST

      afterEach()
    }
  }

  "ApplicationController .update(id: String)" should {

    "update a book in the database by id and data model" in {

      beforeEach()

      val createRequest: FakeRequest[JsValue] = buildGet("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModel))
      val createResult: Future[Result] = TestApplicationController.create()(createRequest)

      status(createResult) shouldBe Status.CREATED

      val updateRequest: FakeRequest[JsValue] = buildPut("/api/${dataModel._id}").withBody[JsValue](Json.toJson(dataModelUpdated))
      val updateResult: Future[Result] = TestApplicationController.update(dataModelUpdated._id)(updateRequest)

      val readResult: Future[Result] = TestApplicationController.read("X Æ A-Xii")(FakeRequest())

      status(updateResult) shouldBe Status.ACCEPTED
      status(readResult) shouldBe Status.OK
      contentAsJson(readResult).as[DataModel] shouldBe dataModelUpdated

      afterEach()

    }

    "throw an BadRequest when given a body with the incorrect format" in {

      beforeEach()

      val updateRequest: FakeRequest[JsValue] = buildPut(s"/api/${dataModel._id}").withBody[JsValue](Json.toJson("noot"))
      val updateResult: Future[Result] = TestApplicationController.update(dataModel._id)(updateRequest)

      status(updateResult) shouldBe Status.BAD_REQUEST

      afterEach()

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

      afterEach()

    }

    "throw an BadRequest when .delete is given an empty Book ID string" in {

      beforeEach()

      val request: FakeRequest[AnyContent] = buildDelete("/api/")

      val readResult: Future[Result] = TestApplicationController.delete("")(request)

      status(readResult) shouldBe Status.BAD_REQUEST

      afterEach()

    }
  }

}
