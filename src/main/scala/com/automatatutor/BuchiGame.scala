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

object BuchiGameSolving {
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

    def create() = {
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
      
      val specificProblem : Task = Task.create
      specificProblem.setGeneralProblem(unspecificProblem).setArena(arena)
      specificProblem.save
      
      returnFunc()
    }
    
    val arenaField = SHtml.hidden(arena = _, "", "id" -> "arenaField")
    val submitButton = SHtml.submit("Create", create, "onClick" -> "document.getElementById('arenaField').value = Editor.canvas.exportAutomaton()")

    val shortDescriptionField = SHtml.text("", shortDescription = _)
    val longDescriptionField = SHtml.textarea("", longDescription = _, "cols" -> "80", "rows" -> "5")
    
    val template : NodeSeq = TemplateLoader.BuchiSolving.create
    Helpers.bind("createform", template,
        "arena" -> arenaField,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }

}