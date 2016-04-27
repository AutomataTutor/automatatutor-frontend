package com.automatatutor.snippet

import java.util.Date

import scala.xml.NodeSeq
import scala.xml.Text

import com.automatatutor.lib.TableHelper
import com.automatatutor.model.Problem
import com.automatatutor.model.ProblemType
import com.automatatutor.model.SolutionAttempt
import com.automatatutor.model.User
import com.automatatutor.renderer.ProblemRenderer

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.AnyVar.whatVarIs

object chosenProblem extends RequestVar[Problem](null)
object chosenProblemType extends RequestVar[ProblemType](null)

class Problems {
  def renderindex ( ignored : NodeSeq ) : NodeSeq = {

    val usersProblems = Problem.findAllByCreator(User.currentUser openOrThrowException("We should only be on this page if there is a user logged in"))

    val completeTable = TableHelper.renderTableWithHeader(usersProblems,
        ("Problem Type", (problem : Problem) => Text(problem.getTypeName)),
        ("Description", (problem : Problem) => Text(problem.shortDescription.is)),
        ("", (problem : Problem) => new ProblemRenderer(problem).renderSharingWidget),
        ("", (problem : Problem) => new ProblemRenderer(problem).renderTogglePublicLink),
        ("", (problem : Problem) => new ProblemRenderer(problem).renderDeleteLink))
    
    val creationLink = SHtml.link("/problems/create", () => {}, Text("Create new problem"))
    
    return completeTable ++ creationLink
  }
  
  def rendercreate( ignored : NodeSeq ) : NodeSeq = {
    val problemType = chosenProblemType.is

    def createUnspecificProb ( shortDesc : String, longDesc : String) : Problem = {
      val createdBy : User = User.currentUser openOrThrowException "Lift protects this page against non-logged-in users"

      val unspecificProblem : Problem = Problem.create.createdBy(createdBy).visibility(1)
      unspecificProblem.shortDescription(shortDesc).longDescription(longDesc).problemType(problemType)
      unspecificProblem.save

      return unspecificProblem
    }
    
    def returnFunc() = { 
      S.redirectTo("/problems/index")
    }

    if(problemType == null) {
      return renderTypeMenu()
    } else {
      return problemType.getProblemSnippet().renderCreate(createUnspecificProb, returnFunc)
    }
  }
  
  def renderedit( ignored : NodeSeq ) : NodeSeq = {
    if(chosenProblem == null) {
      S.warning("Please choose a problem to edit first")
      return S.redirectTo("/problems/index")
    } else {
      val problem : ProblemType = (chosenProblem.problemType.obj openOrThrowException "chosenProblem should have been set beforehand")
      val problemSnippet : ProblemSnippet = problem.getProblemSnippet()
      return problemSnippet.renderEdit(chosenProblem)
    }
  }

  def renderTypeMenu() : NodeSeq = {
    val headerLine = <h2> Choose Problem Type </h2>

    val knownProblemTypes = ProblemType.findAll.map(problemType => <li> {
      SHtml.link("/problems/create", () => chosenProblemType(problemType), Text(problemType.getProblemTypeName))
    }
    </li> )
    val knownProblemTypesList = <ul> { knownProblemTypes } </ul>
    
    val returnLink = SHtml.link("/problems/index", () => {}, Text("Back to index"))

    return headerLine ++ knownProblemTypesList ++ returnLink
  }
  
  def renderpublicproblems( ignored : NodeSeq ) : NodeSeq = {
    val publicProblems = Problem.findPublicProblems

    return TableHelper.renderTableWithHeader(publicProblems, 
        ("Short Description", (problem : Problem) => Text(problem.shortDescription.is)),
        ("Category", (problem : Problem) => Text(problem.getTypeName)),
        ("", (problem : Problem) => SHtml.link("/preview/solve", () => chosenProblem.set(problem), Text("Solve"))))
  }
  
  def rendersolvepublicproblem( ignored : NodeSeq ) : NodeSeq = {
    val problem = chosenProblem.is
    if(problem == null) { 
      S.redirectTo("/preview/index", 
          () => S.notice("Please login. We temporarily disabled the Try it Now problems."))
    }
    val snippet = problem.getType.getProblemSnippet
    return snippet.renderSolve(problem, 10, Empty, 
        (date, grade) => SolutionAttempt, 
        	() => S.redirectTo("/preview/index"), 
            () => 1, 
            () => 100)
  }
}

trait ProblemSnippet {
  /** Should produce a NodeSeq that allows the user to create a new problem of
   *  the type. This NodeSeq also has to handle creation of the unspecific
   *  {@link Problem}. */
  def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq

  /** Should produce a NodeSeq that allows the user to edit the problem
   *  associated with the given unspecific problem. */
  def renderEdit( problem : Problem ) : NodeSeq
  
  /** Should produce a NodeSeq that allows the user a try to solve the problem
   *  associated with the given unspecific problem. The function
   *  recordSolutionAttempt must be called once for every solution attempt
   *  and expects the grade of the attempt (which must be <= maxGrade) and the
   *  time the attempt was made. After finishing the solution attempt, the
   *  snippet should send the user back to the overview of problems in the
   *  set by calling returnToSet */
  def renderSolve ( problem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt: (Int, Date)  => SolutionAttempt,
      returnToSet : () => Unit , attemptsLeft : () => Int, bestGrade : () => Int) : NodeSeq
  
  /** Is called before the given unspecific problem is deleted from the database.
   *  This method should delete everything associated with the given unspecific
   *  problem from the database */
  def onDelete( problem : Problem ) : Unit
}