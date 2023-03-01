package services

import models.{APIError, DataModel}
import org.mongodb.scala.result
import org.mongodb.scala.result.UpdateResult
import play.api.mvc.Result
import repositories.DataRepositoryTrait

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RepositoryService @Inject()(val dataRepositoryTrait: DataRepositoryTrait){

  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]] =
    dataRepositoryTrait.index()

  def create(book: DataModel): Future[Either[Result, DataModel]] =
    dataRepositoryTrait.create(book)

  def read(id: String): Future[Either[Result, DataModel]] =
    dataRepositoryTrait.read(id)

  def readByTitle(id: String): Future[Either[Result, DataModel]] =
    dataRepositoryTrait.readByTitle(id)

  def update(id: String, book: DataModel): Future[Either[Result, UpdateResult]] =
    dataRepositoryTrait.update(id, book)

  def updateByFieldAndUpdate(id: String, field: String, update: String): Future[Either[Result, result.UpdateResult]] =
    dataRepositoryTrait.updateByFieldAndUpdate(id, field, update)

  def delete(id: String): Future[Either[Result, result.DeleteResult]] =
    dataRepositoryTrait.delete(id)
}
