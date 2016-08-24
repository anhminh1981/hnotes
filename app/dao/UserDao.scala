package dao

import javax.inject.Inject
import scala.concurrent.Future

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile


import models.User
import play.api.Logger

class UserDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  
  private val Users = TableQuery[UsersTable]
    
  def all(): Future[Seq[User]] = db.run(Users.result)
  
  def selectByEmail(email: String): Future[Seq[User]] = {
    val query = Users.filter (_.email === email )
    db.run(query.result )
  }

  def insert(user: User): Future[Unit] = {
    Logger.debug("Inserting " + user)
    db.run(Users += user).map { _ => () }
  }

  private class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL")
    def password = column[String]("PASSWORD")
    def role = column[String]("ROLE")
    
    def * = (id, email, password, role) <> (User.tupled, User.unapply _)
  }
}