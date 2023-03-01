package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}

case class DataModel(_id: String,
                     name: String,
                     description: String,
                     numSales: Int)

object DataModel {
  implicit val formats: OFormat[DataModel] = Json.format[DataModel]
  val bookForm: Form[DataModel] = Form(
    mapping(
      "_id" -> text,
      "name" -> text,
      "description" -> text,
      "numSales" -> number
    )(DataModel.apply)(DataModel.unapply)
  )
}