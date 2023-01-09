package models

case class Book(id: String,
                volumeInfo: VolumeInfo,
                publisher: String,
                publishedDate: String

  )

case class VolumeInfo(title: String,
                      subtitle: String,
                      authors: List[String])

