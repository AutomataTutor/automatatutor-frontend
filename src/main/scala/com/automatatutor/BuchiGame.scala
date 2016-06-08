package com.automatatutor

import scala.xml.NodeSeq
import com.automatatutor.lib.TemplateLoader
import com.automatatutor.model.DFAConstructionProblem
import com.automatatutor.model.Problem
import com.automatatutor.model.SolutionAttempt
import net.liftweb.http.SHtml
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedLongForeignKey
import net.liftweb.mapper.MappedText
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import com.automatatutor.lib.Renderer
import com.automatatutor.lib.Binding
import com.automatatutor.lib.Binder
import com.automatatutor.snippet.ProblemSnippet
import java.util.Date
import net.liftweb.common.Box

object BuchiGameSolving {
  object SnippetAdapter extends ProblemSnippet {
    /** Should produce a NodeSeq that allows the user to create a new problem of
     *  the type. This NodeSeq also has to handle creation of the unspecific
     *  {@link Problem}. */
    def renderCreate( createUnspecificProb : (String, String) => Problem, returnFunc : () => Nothing ) : NodeSeq = BuchiGameSolving.renderCreate(createUnspecificProb, returnFunc)

    /** Should produce a NodeSeq that allows the user to edit the problem
     *  associated with the given unspecific problem. */
    def renderEdit : Box[((Problem, () => Nothing) => NodeSeq)] = ???
    
    /** Should produce a NodeSeq that allows the user a try to solve the problem
     *  associated with the given unspecific problem. The function
     *  recordSolutionAttempt must be called once for every solution attempt
     *  and expects the grade of the attempt (which must be <= maxGrade) and the
     *  time the attempt was made. After finishing the solution attempt, the
     *  snippet should send the user back to the overview of problems in the
     *  set by calling returnToSet */
    def renderSolve ( problem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
        recordSolutionAttempt: (Int, Date)  => SolutionAttempt,
        returnToSet : () => Unit , attemptsLeft : () => Int, bestGrade : () => Int) : NodeSeq = ???
    
    /** Is called before the given unspecific problem is deleted from the database.
     *  This method should delete everything associated with the given unspecific
     *  problem from the database */
    def onDelete( problem : Problem ) : Unit = ???
    
  }
  class Task extends LongKeyedMapper[Task] with IdPK {
    def getSingleton = Task
    
    protected object generalProblem extends MappedLongForeignKey(this, Problem)
    protected object arena extends MappedText(this)
    
    def setGeneralProblem ( problem : Problem) : Task = this.generalProblem(problem)
    def getGeneralProblem : Problem = this.generalProblem.obj openOrThrowException "Every Task must have a generalProblem"
    
    def setArena ( game : String ) = this.arena(game)
    def setArena ( game : NodeSeq ) = this.arena(game.mkString)
    def getArena : String = this.arena.get
  }
  object Task extends Task with LongKeyedMetaMapper[Task]
  
  class Solution extends LongKeyedMapper[Solution] with IdPK {
    def getSingleton = Solution
    
    protected object generalSolution extends MappedLongForeignKey(this, SolutionAttempt)
    protected object solution extends MappedText(this)
  }
  object Solution extends Solution with LongKeyedMetaMapper[Solution]
  
  def renderCreate( createUnspecificProb : (String, String) => Problem, returnFunc : () => Nothing ) : NodeSeq = {
    var shortDescription : String = ""
    var longDescription : String = ""
    var arena : String = ""

    object canvasRenderer extends Renderer { def render = {
      <lift:embed what="/applets/buchi-game"> </lift:embed> ++
      SHtml.hidden(arena = _, "", "id" -> "arenaField")
    } }
    object submitButtonRenderer extends Renderer { def render = {
      SHtml.submit("Create", create, "onClick" -> "document.getElementById('arenaField').value = Editor.canvas.exportAutomaton()")
    } }
    object shortDescRenderer extends Renderer { def render = {
      SHtml.text("", shortDescription = _)
    } }
    object longDescRenderer extends Renderer { def render = {
      SHtml.textarea("", longDescription = _, "cols" -> "80", "rows" -> "5")
    } }

    def create() = {
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
      
      val specificProblem : Task = Task.create
      specificProblem.setGeneralProblem(unspecificProblem).setArena(arena)
      specificProblem.save
      
      returnFunc()
    }
    
    val arenaBinding = new Binding("arena", canvasRenderer)
    val submitBinding = new Binding("submit", submitButtonRenderer)

    val shortDescBinding  = new Binding("shortdescription", shortDescRenderer)
    val longDescBinding = new Binding("longdescription", longDescRenderer)
    
    val template : NodeSeq = TemplateLoader.BuchiSolving.create
    new Binder("createform", arenaBinding, submitBinding, shortDescBinding, longDescBinding).bind(template)
  }

}