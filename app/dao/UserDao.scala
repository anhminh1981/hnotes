package dao

import javax.inject.Inject
import scala.concurrent.Future

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import com.github.t3hnar.bcrypt._

import models.User
import play.api.Logger
import scala.util.Try
import scala.util.Failure
import dao.exception.InsertDuplicateException
import scala.concurrent.ExecutionContext

class UserDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  
  private val Users = TableQuery[UsersTable]
    
  def all(): Future[Seq[User]] = db.run(Users.result)
  
  def selectByEmail(email: String): Future[Option[User]] = {
    Logger.debug(s"Looking for $email")
    val query = Users.filter (_.email === email )
    db.run(query.result ) map { seq => if(seq.isEmpty) None else Some(seq.head)}
  }

  def insert(user: User): Future[Try[User]] = {
    Logger.debug("Inserting user " + user.email)
    val ins = (for {
      existing <- (Users filter(_.email === user.email)).result if existing.isEmpty
      newId <- (Users returning Users.map(_.id) )+= user
    } yield User(newId, user.email, user.password, user.role)).transactionally
    
    val r = db.run(ins.asTry)
    
    r map { _ recoverWith {
    case e => if(e.isInstanceOf[NoSuchElementException]) Failure(InsertDuplicateException("USERS", "email", user.email)) else Failure(e)
    }}
  }

  private class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL")
    def password = column[String]("PASSWORD")
    def role = column[String]("ROLE")
    
    def crypt(user: User): User = user.copy(password = user.password.bcrypt)
    
    def * = (id, email, password, role) <> (User.tupled, User.unapply _ compose crypt _)
  }
}