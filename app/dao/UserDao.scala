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
  
  def selectByEmail(email: String): Future[Option[User]] = {
    val query = Users.filter (_.email === email )
    db.run(query.result ) map { seq => if(seq.isEmpty) None else Some(seq.head)}
  }

  def insert(user: User): Future[User] = {
    Logger.debug("Inserting " + user)
    db.run((Users returning Users.map(_.id) )+= user).map { User(_, user.email, user.password, user.role) }
  }

  private class UsersTable(tag: Tag) extends Table[User](tag, "Users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def password = column[String]("password")
    def role = column[String]("role")
    
    def * = (id, email, password, role) <> (User.tupled, User.unapply _)
  }
}