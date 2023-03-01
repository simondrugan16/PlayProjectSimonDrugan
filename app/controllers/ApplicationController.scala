package controllers

import models.{Book, DataModel}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Result, Results}
import play.filters.csrf.CSRF
import services.{LibraryService, RepositoryService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class ApplicationController @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext, val service: LibraryService, val repositoryService: RepositoryService)
  extends BaseController with play.api.i18n.I18nSupport {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.index().map {
      case Right(item: Seq[DataModel]) => Ok {
        Json.toJson(item)
      }
      case Left(error) => Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel] match {
      case JsSuccess(dataModel, _) =>
        repositoryService.create(dataModel).map {
          case Left(error) => error
          case Right(result) => Created
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    validateEmpty(id) match {
      case Left(error) => Future(error)
      case Right(validatedId) =>
        for {
          book <- repositoryService.read(validatedId)
          res = book match {
            case Right(dataModel) => Ok(Json.toJson(dataModel))
            case Left(error) => error
          }} yield
          res
    }
  }

  def readByTitle(title: String): Action[AnyContent] = Action.async { implicit request =>
    validateEmpty(title) match {
      case Left(error) => Future(error)
      case Right(validatedTitle) =>
        for {
          book <- repositoryService.readByTitle(validatedTitle)
          res = book match {
            case Right(dataModel) => Ok(Json.toJson(dataModel))
            case Left(error) => error
          }} yield
          res
    }
  }

  def validateEmpty(id: String): Either[Result, String] = {
    if (id.isEmpty)
      Left(BadRequest("ID empty"))
    else
      Right(id)
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[DataModel]
    match {
      case JsSuccess(dataModel, _) =>
        repositoryService.update(id, dataModel).map{
          case Left(error) => error
          case Right(result) => Accepted
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def updateByFieldAndUpdate(id: String, field: String, update: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    //request.body.validate[Seq[String]]
    repositoryService.updateByFieldAndUpdate(id, field, update).map {
      case Left(error) => error
      case Right(result) => Accepted
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    validateEmpty(id) match {
      case Left(error) => Future(error)
      case Right(validatedId) =>
        for {
          book <- repositoryService.delete(validatedId)
          res = book match {
            case Right(dataModel) => Accepted
            case Left(error) => error
          }} yield
          res
    }
  }

  def getGoogleBook(search: String, term: String): Action[AnyContent] = Action.async { implicit request =>
    service.getGoogleBook(search = search, term = term).value.map {
      case Right(book: Book) => Ok(Json.toJson(book))
      case Left(error) => Results.Status(error.httpResponseStatus)(Json.toJson(error.reason))
    }
  }

  def example(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.example(DataModel("1", name = "NOTW", description = "Good book", numSales = 1))))
  }

  def accessToken(implicit request: Request[_]) = {
    CSRF.getToken
  }

  def addBook: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.AddANewBook(DataModel.bookForm))
  }

  def addBookForm(): Action[AnyContent] = Action.async { implicit request =>
    accessToken
    DataModel.bookForm.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(formWithErrors.toString))
      },
      formData => {
        repositoryService.create(formData).map {
          case Right(book) => Created
          case Left(error) => InternalServerError
        }
      }
    )
  }
}
