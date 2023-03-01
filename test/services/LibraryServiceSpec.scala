package services

import baseSpec.BaseSpec
import cats.data.EitherT.{leftT, rightT}
import connectors.LibraryConnector
import models.{APIError, Book}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json, OFormat}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class LibraryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite {

  val mockConnector: LibraryConnector = mock[LibraryConnector]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val testService = new LibraryService(mockConnector)

  val gameOfThrones: JsValue = Json.obj(
    "id" -> "someId",
    "volumeInfo" -> "A Game of Thrones",
    "publisher" -> "The best book!!!",
    "publishedDate" -> "LAST WEEK"
  )

  "getGoogleBook" should {
    val url: String = "testUrl"

    "return a book" in {
      (mockConnector.get[Book](_: String)(_: OFormat[Book], _: ExecutionContext))
        .expects(url, *, *)
        .returning(rightT(gameOfThrones.as[Book]))
        .once()

      val myBook = Await.result(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "").value, 2.minute)

      myBook match {
        case Left(value) => fail(s"This failed with unexpected value $value")
        case Right(value) => value shouldBe(gameOfThrones.as[Book])
      }

    }

    "return an error" in {
      val url: String = "testUrl"

      (mockConnector.get[Book](_: String)(_: OFormat[Book], _: ExecutionContext))
        .expects(url, *, *)
        .returning(leftT[Future, Book](APIError.BadAPIResponse(1, "COULD NOT RETRIEVE")))
        .once()

      whenReady(testService.getGoogleBook(urlOverride = Some(url), search = "", term = "").value) {
        case Left(value) => value.httpResponseStatus shouldBe(Status.INTERNAL_SERVER_ERROR)
        case Right(value) => fail(s"This failed with unexpected value $value")
      }
    }

  }

}
