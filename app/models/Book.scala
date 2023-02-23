package models

import play.api.libs.json.{Json, OFormat}

case class Book(id: String,
                volumeInfo: String,
                publisher: String,
                publishedDate: String

  )

object Book {
  implicit val formats: OFormat[Book] = Json.format[Book]
}

case class VolumeInfo(title: String,
                      subtitle: String,
                      authors: List[String])

object VolumeInfo {
  implicit val formats: OFormat[VolumeInfo] = Json.format[VolumeInfo]
}

case class CollectionOfBooks(books: List[Book])

object CollectionOfBooks {
  implicit val formats: OFormat[CollectionOfBooks] = Json.format[CollectionOfBooks]
}

