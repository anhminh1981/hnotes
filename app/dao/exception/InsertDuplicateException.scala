package dao.exception

case class InsertDuplicateException(table: String, column: String, value: Any) extends Exception {
  
}