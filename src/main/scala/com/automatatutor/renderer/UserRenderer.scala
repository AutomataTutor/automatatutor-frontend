package com.automatatutor.renderer

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.SHtml
import scala.xml.NodeSeq
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import scala.xml.Text
import com.automatatutor.model.User

class UserRenderer(user : User) {
  
  def renderDeleteLink : NodeSeq = {
    val target = "/users/index"
    def function() = user.delete_!
    val label = if(user.canBeDeleted) { Text("Delete") } else { Text("Cannot delete user") }
    val onclick : JsCmd = if(user.canBeDeleted) { 
        JsRaw("return confirm('Are you sure you want to delete this user?')") 
      } else  { 
        JsCmds.Alert("Cannot delete this user:\n" + user.getDeletePreventers.mkString("\n")) & JsRaw("return false")
      }

    return SHtml.link(target, function, label, "onclick" -> onclick.toJsCmd)
  }

}