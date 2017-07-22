package com.automatatutor.snippet

import scala.xml.NodeSeq
import scala.xml.Text
import com.automatatutor.lib.TableHelper
import com.automatatutor.model.PosedProblem
import com.automatatutor.model.Problem
import com.automatatutor.model.ProblemSet
import com.automatatutor.model.User
import com.automatatutor.renderer.ProblemSetRenderer
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._
import com.automatatutor.lib.Binding
import com.automatatutor.lib.Renderer
import com.automatatutor.lib.Binder
import com.automatatutor.lib.DataRenderer
import scala.collection.mutable.Set;


object ProblemSetToEdit extends RequestVar[ProblemSet](null)
object ProblemsToPose extends RequestVar[Set[Problem]](Set())

class Problemsets {
	def renderindex ( template : NodeSeq ) : NodeSeq = {
	  
	  val problemSets : Seq[ProblemSet] = ProblemSet.getByCreator(User.currentUser openOrThrowException "Lift only lets logged in users onto this site")

	  object problemSetRenderer extends DataRenderer(problemSets) {
	    def render( template : NodeSeq, problemSet : ProblemSet ) : NodeSeq = {
	      object nameRenderer extends Renderer { def render : NodeSeq = Text(problemSet.getName) }
	      object togglePracticeSetLinkRenderer extends Renderer { def render : NodeSeq = new ProblemSetRenderer(problemSet).renderTogglePracticeSetLink }
	      object editLinkRenderer extends Renderer { def render : NodeSeq = new ProblemSetRenderer(problemSet).renderEditLink }
	      object deleteLinkRenderer extends Renderer { def render : NodeSeq = new ProblemSetRenderer(problemSet).renderDeleteLink }
	      
	      val nameBinding = new Binding("name", nameRenderer)
	      val togglePracticeSetLinkBinding = new Binding("togglepracticesetlink", togglePracticeSetLinkRenderer)
	      val editLinkBinding = new Binding("editlink", editLinkRenderer)
	      val deleteLinkBinding = new Binding("deletelink", deleteLinkRenderer)
	      
	      return new Binder("problemset", nameBinding, togglePracticeSetLinkBinding, editLinkBinding, deleteLinkBinding).bind(template)
	    }
	  }
	  
	  Helpers.bind("problemsets", template,
	    "foreach" -> { template : NodeSeq => problemSetRenderer.render(template) },
	    "createnewlink" -> { template : NodeSeq => SHtml.link("/problemsets/create", () => {}, Text(template(0).text)) } )
	}
	
	def rendercreate ( xhtml : NodeSeq ) : NodeSeq = {
	  var name : String = ""
	  
	  def create() = {
	    ProblemSet.create.setName(name).setCreatedBy(User.currentUser_!).save
	  }
	  
	  object nameFieldRenderer extends Renderer { def render = {
      SHtml.text("", name = _, "maxlength" -> "50")
	  } }
	  object createButtonRenderer extends Renderer { def render = {
	    SHtml.submit("Create", () => { create(); S.redirectTo("/problemsets/index") })
	  } }
	  val nameFieldBinding = new Binding("namefield", nameFieldRenderer)
	  val createButtonBinding = new Binding("createbutton", createButtonRenderer)
	  
	  new Binder("problemsetcreate", nameFieldBinding, createButtonBinding).bind(xhtml)
	}
	
	def renderedit ( xhtml : NodeSeq ) : NodeSeq = {
	  val problemSetToEdit : ProblemSet = ProblemSetToEdit.is
	  val posedProblems = problemSetToEdit.getPosedProblems
	  
	  val posedProblemsTable = TableHelper.renderTableWithHeader(posedProblems,
	      ("Short Description", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.getShortDescription)),
	      ("Problem Type", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.getTypeName)),
	      ("Allowed Attempts", (posedProblem : PosedProblem) => Text(posedProblem.getAllowedAttempts.toString() + " Attempts allowed")),
	      ("", (posedProblem : PosedProblem) => SHtml.link( "/problemsets/index", () => { problemSetToEdit.removeProblem(posedProblem) }, Text("Remove problem from set")
	          )
	        )
	      )
	  
	  val addProblemLink = SHtml.link("/problemsets/addproblem", () => ProblemSetToEdit(problemSetToEdit), Text("Append new problem"))
	  
	  return posedProblemsTable ++ addProblemLink
	}
	
	def renderaddproblem ( xhtml : NodeSeq ) : NodeSeq = {
	  val problemSetToEdit = ProblemSetToEdit.is
	  val problemsToPose = ProblemsToPose.is
	  val currentUser = User.currentUser openOrThrowException "Lift prevents non-logged in users from getting here"
	  val problems : Seq[Problem] = Problem.findAllByCreator(currentUser)
	  
	  def checkAndSubmit() {
		if (problemsToPose.size > 0) S.redirectTo("/problemsets/poseproblem", () => {ProblemSetToEdit(problemSetToEdit); ProblemsToPose(problemsToPose) } )
		else S.error("No problem selected");
	  }
	  
	  var addProblemTable = TableHelper.renderTableWithHeader(problems,
	      ("Short Description", (problem : Problem) => Text(problem.getShortDescription)),
	      ("Problem Type", (problem : Problem) => Text(problem.getTypeName)),
		  ("Selection", (problem : Problem) => SHtml.checkbox(false, (selected) => {if (selected) problemsToPose += problem} ) )
		  ) 
		  
	  var addSelectedProblemsSubmitButton = SHtml.submit("Pose selected problems", checkAndSubmit )
	  
	  return addProblemTable ++ addSelectedProblemsSubmitButton
	}
	
	def renderposeproblem ( xhtml : NodeSeq ) : NodeSeq = {
	  val problemSetToEdit = ProblemSetToEdit.is
	  val problemsToPose = ProblemsToPose.is
	  
	  var attempts = ""
	  var maxGrade = ""
	    
	  def poseProblem = {
	    def redirectToSelf( exception : Exception ) = {
	        S.error(exception.getMessage())
	        S.redirectTo("/problemsets/poseproblem", () => { ProblemSetToEdit(problemSetToEdit) } )
	    }
		val numAttempts = try { attempts.toInt } catch { case e : Exception => redirectToSelf(e) }
		val numMaxGrade = try { maxGrade.toInt } catch { case e : Exception => redirectToSelf(e) }
		problemsToPose.foreach(
			(problem) => {
				problemSetToEdit.appendProblem( problem, numAttempts, numMaxGrade )
			} 
		)
	    S.redirectTo("/problemsets/edit", () => ProblemSetToEdit(problemSetToEdit))
	  }
	  
	  object maxGradeFieldRenderer extends Renderer { def render = {
	    SHtml.text("10", maxGrade = _)
	  } }
	  object attemptsFieldRenderer extends Renderer { def render = {
	    SHtml.text("3", attempts = _)
	  } }
	  object submitButtonRenderer extends Renderer { def render = {
	    SHtml.submit("Pose Problem", () => poseProblem)
	  } }
	  val maxGradeFieldBinding = new Binding("maxgradefield", maxGradeFieldRenderer)
	  val attemptsFieldBinding = new Binding("attemptsfield", attemptsFieldRenderer)
	  val poseButtonBinding = new Binding("posebutton", submitButtonRenderer)

	  new Binder("poseproblemform", maxGradeFieldBinding, attemptsFieldBinding, poseButtonBinding).bind(xhtml)
	}
}