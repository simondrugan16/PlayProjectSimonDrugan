package repositories

import com.google.inject.ImplementedBy
import models.{APIError, DataModel}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.empty
import org.mongodb.scala.model._
import org.mongodb.scala.result
import org.mongodb.scala.result.UpdateResult
import play.api.libs.json.JsSuccess
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

@ImplementedBy(classOf[DataRepository])
trait DataRepositoryTrait {
  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]]
  def create(book: DataModel): Future[Either[Result, DataModel]]
  def read(id: String): Future[Either[Result, DataModel]]
  def readByTitle(title: String): Future[Either[Result, DataModel]]
  def update(id: String, book: DataModel): Future[Either[Result, UpdateResult]]
  def makeNewModel(id: String, field: String, update: String): Future[Either[Result, DataModel]]
  def updateByFieldAndUpdate(id: String, field: String, update: String): Future[Either[Result, result.UpdateResult]]
  def delete(id: String): Future[Either[Result, result.DeleteResult]]
  def deleteAll(): Future[Unit]
}
    @Singleton
class DataRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[DataModel](
  collectionName = "dataModels",
  mongoComponent = mongoComponent,
  domainFormat = DataModel.formats,
  indexes = Seq(IndexModel(
    Indexes.ascending("_id")
  )),
  replaceIndexes = false
) with DataRepositoryTrait {

  def index(): Future[Either[APIError.BadAPIResponse, Seq[DataModel]]] = {
    collection.find().toFuture().map {
      case books: Seq[DataModel] => Right(books)
      case _ => Left(APIError.BadAPIResponse(500, "Internal Server Error Occurred"))
    }
  }

  def create(book: DataModel): Future[Either[Result, DataModel]] =
    collection
      .insertOne(book).headOption flatMap {
//      .toFuture()
//      .map(_ => book) flatMap {
      case Some(result) => Future(Right(book))
      case None => Future(Left(InternalServerError("ERROR: Could not create an item.")))
    }

  private def byID(id: String): Bson =
    Filters.and(
      Filters.equal("_id", id)
    )

  private def byTitle(title: String): Bson =
    Filters.and(
      Filters.equal("name", title)
    )

  def read(id: String): Future[Either[Result, DataModel]] =
    collection.find(byID(id)).headOption flatMap {
      case Some(data) =>
        Future(Right(data))
      case None => Future(Left(InternalServerError("ERROR: Could not read an item. ID not found in database.")))
    }

  def readByTitle(title: String): Future[Either[Result, DataModel]] =
    collection.find(byTitle(title)).headOption flatMap {
      case Some(data) =>
        Future(Right(data))
      case None => Future(Left(InternalServerError("ERROR: Could not read an item. ID not found in database.")))
    }

  def update(id: String, book: DataModel): Future[Either[Result, result.UpdateResult]] =
    collection.replaceOne(
      filter = byID(id),
      replacement = book,
      options = new ReplaceOptions().upsert(true) //What happens when we set this to false?
    ).headOption flatMap {
      case Some(result) => Future(Right(result))
      case None => Future(Left(InternalServerError("ERROR: Couldn't update database item.")))
    }

  def makeNewModel(id: String, field: String, update: String): Future[Either[Result, DataModel]] = {
    read(id).flatMap {
      case Left(error) => Future(Left(InternalServerError("ERROR: Couldn't update database item.")))
      case Right(book) => field match {
        case name: String => Future(Right(book.copy(name = update)))
        case description: String => Future(Right(book.copy(description = update)))
      }
    }
  }
  def updateByFieldAndUpdate(id: String, field: String, update: String): Future[Either[Result, result.UpdateResult]] = {
    val newModel = Await.result(makeNewModel(id, field, update), 2.minutes)
    newModel match {
      case Left(error) => Future(Left(error))
      case Right(model) => collection.replaceOne(
        filter = byID(id),
        replacement = model,
        options = new ReplaceOptions().upsert(true) //What happens when we set this to false?
      ).headOption flatMap {
        case Some(result) => Future(Right(result))
        case None => Future(Left(InternalServerError("ERROR: Couldn't update database item.")))
      }
    }
  }

  def delete(id: String): Future[Either[Result, result.DeleteResult]] =
    collection.deleteOne(filter = byID(id)).headOption flatMap {
      case Some(result) => Future(Right(result))
      case None => Future(Left(InternalServerError("ERROR: ID not found in database")))
    }

  def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ()) //Hint: needed for tests

}

