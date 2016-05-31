package com.automatatutor.renderer

import scala.xml.NodeSeq
import scala.xml.Text

import com.automatatutor.model.Problem
import com.automatatutor.model.User
import com.automatatutor.snippet.chosenProblem

import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._

class ProblemRenderer(problem : Problem) {
  def renderDeleteLink : NodeSeq = {
    val target = "/problems/index"
    def function() = { problem.getProblemType.getProblemSnippet().onDelete(problem); problem.delete_! }
    val label = if(problem.canBeDeleted) { Text("Delete") } else { Text("Cannot delete Problem") }
    val onclick : JsCmd = if(problem.canBeDeleted) { 
        JsRaw("return confirm('Are you sure you want to delete this problem?')") 
      } else  { 
        JsCmds.Alert("Cannot delete this problem:\n" + problem.getDeletePreventers.mkString("\n")) & JsRaw("return false")
      }

    return SHtml.link(target, function, label, "onclick" -> onclick.toJsCmd)
  }
  
  def renderTogglePublicLink : NodeSeq = {
    if(User.currentUser_!.hasAdminRole) {
      val linkText = if(problem.isPublic) { Text("Make private") } else {Text("Make public")}
      return SHtml.link("/problems/index", () => problem.toggleVisibility.save(), linkText)
    } else {
      return NodeSeq.Empty
    }
  }
  
  def renderSharingWidget : NodeSeq = {
    def shareWithFeedback(email: String) = {
      if (problem.shareWithUserByEmail(email)) { S.notice("Successfully shared with " + email) } 
      else { S.error("Could not find user " + email) }
    }
    <form action="/problems/index"> { SHtml.text("", shareWithFeedback(_)) } <input type="submit" value="Share"/> </form>
  }
  
  def renderEditLink : NodeSeq = {
    problem.getProblemType.getProblemSnippet().renderEdit match {
      case Full(_) => SHtml.link("/problems/edit", () => chosenProblem(problem), Text("Edit"))
      case Empty => NodeSeq.Empty
      case _ => S.error("Error when retrieving editing function"); S.redirectTo("/problems/index")
    }
  }
}