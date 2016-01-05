package com.automatatutor.renderer

import scala.xml.NodeSeq
import scala.xml.Text

import com.automatatutor.model.ProblemSet
import com.automatatutor.model.User
import com.automatatutor.snippet.ProblemSetToEdit

import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.jsExpToJsCmd

class ProblemSetRenderer(problemSet : ProblemSet) {
  def renderEditLink : NodeSeq = {
    val target = "/problemsets/edit"
    def function() = ProblemSetToEdit(problemSet)
    val label = if(problemSet.canBeEdited) { Text("Edit") } else { Text("Cannot edit Problem Set") }
    val onclick : JsCmd = if(!problemSet.canBeEdited) { 
        JsCmds.Alert("Cannot edit this problem set:\n" + problemSet.getEditPreventers.mkString("\n")) & JsRaw("return false")
      } else  { 
        JsCmds.Noop
      }

    return SHtml.link(target, function, label, "onclick" -> onclick.toJsCmd)
  }

  def renderDeleteLink : NodeSeq = {
    val target = "/problemsets/index"
    def function() = problemSet.delete_!
    val label = if(problemSet.canBeDeleted) { Text("Delete") } else { Text("Cannot delete Problem Set") }
    val onclick : JsCmd = if(problemSet.canBeDeleted) { 
        JsRaw("return confirm('Are you sure you want to delete this problem set?')") 
      } else  { 
        JsCmds.Alert("Cannot delete this problem set:\n" + problemSet.getDeletePreventers.mkString("\n")) & JsRaw("return false")
      }

    return SHtml.link(target, function, label, "onclick" -> onclick.toJsCmd)
  }
  
  def renderTogglePracticeSetLink : NodeSeq = {
    if(User.currentUser_!.hasAdminRole) {
      val linkText = if(problemSet.isPracticeSet) { "Make Nonpractice Set" } else { "Make Practice Set" }
      return SHtml.link("/problemsets/index", () => problemSet.togglePracticeSet, Text(linkText))
    } else {
      return NodeSeq.Empty
    }
  }
}
