package models

import org.joda.time.DateTime

case class Note(id: Long, owner: Long, typeNote: String, title: String, 
    text: String, data: Array[Byte] , createdAt: DateTime, modifiedAt: DateTime )