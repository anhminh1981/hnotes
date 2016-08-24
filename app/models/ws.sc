package models

object ws {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  case class Cat(name: String, color: String)

	Cat.tupled                                //> res0: ((String, String)) => models.ws.Cat = <function1>
	 Cat.unapply _                            //> res1: models.ws.Cat => Option[(String, String)] = <function1>
}