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


object ProblemSetToEdit extends RequestVar[ProblemSet](null)
object ProblemToPose extends RequestVar[Problem](null)

class Problemsets {
	def renderindex ( ignored : NodeSeq ) : NodeSeq = {
	  val problemSets : Seq[ProblemSet] = ProblemSet.getByCreator(User.currentUser openOrThrowException "Lift only lets logged in users onto this site")
	  
	  val completeTable = TableHelper.renderTableWithHeader(problemSets,
	      ("Short Description", (problemSet : ProblemSet) => Text(problemSet.name.is)),
	      ("", (problemSet : ProblemSet) => new ProblemSetRenderer(problemSet).renderTogglePracticeSetLink),
	      ("", (problemSet : ProblemSet) => (new ProblemSetRenderer(problemSet).renderEditLink)),
	      ("", (problemSet : ProblemSet) => (new ProblemSetRenderer(problemSet)).renderDeleteLink))

	  val createNewProblemSetLink = SHtml.link("/problemsets/create", () => {}, Text("Create new Problemset"))
	  return completeTable ++ createNewProblemSetLink
	}
	
	def rendercreate ( xhtml : NodeSeq ) : NodeSeq = {
	  var name : String = ""
	  val nameField = SHtml.text("", name = _, "maxlength" -> "50")
	  
	  def create() = {
	    ProblemSet.create.name(name).createdBy(User.currentUser).save
	  }

	  val createButton = SHtml.submit("Create", () => { create(); S.redirectTo("/problemsets/index") })
	  
	  Helpers.bind("problemsetcreate", xhtml,
	      "namefield" -> nameField,
	      "createbutton" -> createButton)
	}
	
	def renderedit ( xhtml : NodeSeq ) : NodeSeq = {
	  val problemSetToEdit : ProblemSet = ProblemSetToEdit.is
	  val posedProblems = problemSetToEdit.getPosedProblems
	  
	  val posedProblemsTable = TableHelper.renderTableWithHeader(posedProblems,
	      ("Short Description", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.shortDescription.is)),
	      ("Problem Type", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.getTypeName)),
	      ("Allowed Attempts", (posedProblem : PosedProblem) => Text(posedProblem.getAllowedAttempts.toString() + " Attempts allowed")),
	      ("", (posedProblem : PosedProblem) => SHtml.link("/problemsets/edit", () => { ProblemSetToEdit(problemSetToEdit); problemSetToEdit.removeProblem(posedProblem)}, Text("Remove problem from set"))))
	  
	  val addProblemLink = SHtml.link("/problemsets/addproblem", () => ProblemSetToEdit(problemSetToEdit), Text("Append new problem"))
	  
	  return posedProblemsTable ++ addProblemLink
	}
	
	def renderaddproblem ( xhtml : NodeSeq ) : NodeSeq = {
	  val problemSetToEdit = ProblemSetToEdit.is
	  val currentUser = User.currentUser openOrThrowException "Lift prevents non-logged in users from getting here"
	  val problems : Seq[Problem] = Problem.findAllByCreator(currentUser)
	  
	  return TableHelper.renderTableWithHeader(problems,
	      ("Short Description", (problem : Problem) => Text(problem.shortDescription.is)),
	      ("Problem Type", (problem : Problem) => Text(problem.getTypeName)),
	      ("", (problem : Problem) => SHtml.link("/problemsets/poseproblem", () => { ProblemSetToEdit(problemSetToEdit); ProblemToPose(problem) }, Text("Pose this problem"))))
	}
	
	def renderposeproblem ( xhtml : NodeSeq ) : NodeSeq = {
	  val problemSetToEdit = ProblemSetToEdit.is
	  val problemToPose = ProblemToPose.is
	  
	  var attempts = ""
	  var maxGrade = ""
	    
	  def poseProblem = {
	    def redirectToSelf( exception : Exception ) = {
	        S.error(exception.getMessage())
	        S.redirectTo("/problemsets/poseproblem", () => { ProblemSetToEdit(problemSetToEdit); ProblemToPose(problemToPose) } )
	    }
		val numAttempts = try { attempts.toInt } catch { case e : Exception => redirectToSelf(e) }
		val numMaxGrade = try { maxGrade.toInt } catch { case e : Exception => redirectToSelf(e) }
	    problemSetToEdit.appendProblem( problemToPose, numAttempts, numMaxGrade )
	    S.redirectTo("/problemsets/edit", () => ProblemSetToEdit(problemSetToEdit))
	  }
	  
	  val maxGradeField = SHtml.text("10", maxGrade = _)
	  val attemptsField = SHtml.text("3", attempts = _)
	  val submitButton = SHtml.submit("Pose Problem", () => poseProblem)
	  
	  Helpers.bind("poseproblemform", xhtml,
	      "maxgradefield" -> maxGradeField,
	      "attemptsfield" -> attemptsField,
	      "posebutton" -> submitButton)
	}
}