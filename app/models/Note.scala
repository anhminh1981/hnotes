package models

import java.sql.Timestamp

case class Note(id: Long, owner: Long, typeNote: String, title: String, 
    text: String, data: Array[Byte] , createdAt: Timestamp, modifiedAt: Timestamp )