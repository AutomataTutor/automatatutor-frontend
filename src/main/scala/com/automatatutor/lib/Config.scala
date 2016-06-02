package com.automatatutor.lib

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.util.Props

object Config {

  abstract class ConfigParam[T]() { def get: T }

  private case class StringParam(protected val accessor: Accessor) extends ConfigParam[String]() { def get = accessor.access }
  private case class IntParam(protected val accessor: Accessor) extends ConfigParam[Int]() { def get = accessor.access.toInt }

  private abstract class Accessor { def access: String }
  private case class Prop(id: String, fallback: Accessor) extends Accessor {
    def access = Props.get(id) match {
      case Full(prop) => prop
      case Empty      => fallback.access
      case _          => fallback.access
    }
  }
  private case class Default(definition: String) extends Accessor { def access = definition }
  private case object Mandatory extends Accessor { def access = throw new Exception("Improper configuration") }

  object layout {
    def usersPerPage: ConfigParam[Int] = IntParam(Prop("layout.usersperpage", Default("50")))
  }

  object mail {
    def user: ConfigParam[String] = StringParam(Prop("mail.user", Default("")))
    def password: ConfigParam[String] = StringParam(Prop("mail.password", Default("")))
    def from: ConfigParam[String] = StringParam(Prop("mail.from", Default("noreply@automatatutor.com")))
  }

  object db {
    def driver: ConfigParam[String] = StringParam(Prop("db.driver", Mandatory))
    def url: ConfigParam[String] = StringParam(Prop("db.url", Mandatory))
    def user: ConfigParam[Box[String]] = new ConfigParam[Box[String]] { def get = Props.get("db.user") }
    def password: ConfigParam[Box[String]] = new ConfigParam[Box[String]] { def get = Props.get("db.password") }
  }

  object admin {
    def firstname: ConfigParam[String] = StringParam(Prop("admin.firstname", Default("Admin")))
    def lastname: ConfigParam[String] = StringParam(Prop("admin.lastname", Default("Admin")))
    def password: ConfigParam[String] = StringParam(Prop("admin.password", Default("admin")))
    def email: ConfigParam[String] = StringParam(Prop("admin.email", Default("admin@automatatutor.com")))
  }

  object grader {
    def url: ConfigParam[String] = StringParam(Prop("grader.url", Mandatory))
    def methodnamespace: ConfigParam[String] = StringParam(Prop("grader.methodnamespace", Mandatory))
  }
}