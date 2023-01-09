package controllers

import cats.data.EitherT
import models.DataModel
import org.mongodb.scala.result
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Result}
import repositories.DataRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(val controllerComponents: ControllerComponents) (val dataRepository: DataRepository) (implicit val ec: ExecutionContext)
  extends BaseController {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val books: Future[Seq[DataModel]] = dataRepository.collection.find().toFuture()
    books.map(items => Json.toJson(items)).map(result => Ok(result))
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        dataRepository.create(dataModel).map(_ => Created)
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      validatedId <- validateEmpty(id)
      book <- dataRepository.read(validatedId.flatten)
      res = book match {
        case Right(dataModel) => Ok(Json.toJson(dataModel))
        case Left(error) => error
     }} yield
      res
  }

  def validateEmpty(id: String): Future[Either[Result, String]] = {
    if (id.isEmpty) {
      println("PPPPPPPPPPPPPP")
      Future(Left(BadRequest("ID empty")))
    } else {
      println("ZZZZZZZZZZZZZZZZ")
      Future(Right(id))
    }
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel]
    match {
      case JsSuccess(dataModel, _) =>
        dataRepository.update(id, dataModel).map(result => Accepted)
      case JsError(_) => Future(BadRequest)
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.delete(id).map {
      case Right(_) => Accepted
      case Left(_) => BadRequest
    }
  }

}
