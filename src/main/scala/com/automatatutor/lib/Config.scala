package com.automatatutor.lib

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.util.Props

object Config {
  object layout {
    object usersPerPage extends IntParam(Prop("layout.usersperpage", Default("50")))
  }
  
  object mail {
    object user extends StringParam(Prop("mail.user", Default("")))
    object password extends StringParam(Prop("mail.password", Default("")))
    object from extends StringParam(Prop("mail.from", Default("noreply@automatatutor.com")))
  }
  
  object db {
    object driver extends StringParam(Prop("db.driver", Mandatory))
    object url extends StringParam(Prop("db.url", Mandatory))
    object user extends ConfigParam[Box[String]] { def get = Props.get("db.user") }
    object password extends ConfigParam[Box[String]] { def get = Props.get("db.password") }
  }
  
  object admin {
    object firstname extends StringParam(Prop("admin.firstname", Default("Admin")))
    object lastname extends StringParam(Prop("admin.lastname", Default("Admin")))
    object password extends StringParam(Prop("admin.password", Default("admin")))
    object email extends StringParam(Prop("admin.email", Default("admin@automatatutor.com")))
  }
  
  object grader {
    object url extends StringParam(Prop("grader.url", Mandatory))
    object methodnamespace extends StringParam(Prop("grader.methodnamespace", Mandatory))
  }
}

abstract class ConfigParam[T]() { def get : T }
case class StringParam(protected val accessor : Accessor) extends ConfigParam[String]() { def get = accessor.access }
case class IntParam(protected val accessor : Accessor) extends ConfigParam[Int]() { def get =  accessor.access.toInt }

abstract class Accessor { def access : String }
case class Prop(id : String, fallback : Accessor) extends Accessor { 
  def access = Props.get(id) match { 
    case Full(prop) => prop
    case Empty => fallback.access
    case _ => fallback.access
  }
}
case class Default(definition : String) extends Accessor { def access = definition }
case object Mandatory extends Accessor { def access = throw new Exception("Improper configuration") }