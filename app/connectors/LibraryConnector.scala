package connectors

import cats.data.EitherT
import models.{APIError, Book}
import play.api.libs.json.{JsError, JsSuccess, OFormat}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest

class LibraryConnector @Inject()(ws: WSClient) {
  def get[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): EitherT[Future, APIError, Response] = {
    val request = ws.url(url)
    val response = request.get()
    EitherT {
      response
        .map {
          result =>
            Right(result.json.as[Response])
        }
        .recover { case _: WSResponse =>
          Left(APIError.BadAPIResponse(500, "Could not connect"))
        }
    }
  }
}
