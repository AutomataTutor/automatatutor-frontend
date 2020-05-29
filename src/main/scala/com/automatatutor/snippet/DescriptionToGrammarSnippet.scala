package com.automatatutor.snippet

import java.util.Calendar
import java.util.Date
import scala.Array.canBuildFrom
import scala.Array.fallbackCanBuildFrom
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Text
import scala.xml.XML
import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.Problem
import com.automatatutor.model.DescriptionToGrammarProblem
import com.automatatutor.model.DescriptionToGrammarSolutionAttempt
import com.automatatutor.model.SolutionAttempt
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.Templates
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.JsHideId
import net.liftweb.http.js.JsCmds.JsShowId
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.http.js.JE.Call
import net.liftweb.common.Empty

object DescriptionToGrammarSnippet extends ProblemSnippet {

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq = {

    def create(formValues : String) : JsCmd = {
      val formValuesXml = XML.loadString(formValues)
      val grammar = (formValuesXml \ "grammarfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text
      
      val parsingErrors = GraderConnection.getGrammarParsingErrors(grammar)
      
      if(parsingErrors.isEmpty) {
        val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
		
        val specificProblem : DescriptionToGrammarProblem = DescriptionToGrammarProblem.create
        specificProblem.problemId(unspecificProblem).grammar(grammar)
        specificProblem.save
              
        return JsCmds.RedirectTo("/problems/index")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }
      
    }
    val grammarField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
    val shortDescriptionField = SHtml.text("", value => {}, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "longdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val grammarFieldValXmlJs : String = "<grammarfield>' + document.getElementById('grammarfield').value + '</grammarfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + grammarFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), create(_))
    val submit : JsCmd = hideSubmitButton & ajaxCall
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={submit}>Submit</button>
	
    val template : NodeSeq = Templates(List("description-to-grammar-problem", "create")) openOr Text("Could not find template /description-to-grammar-problem/create")
    Helpers.bind("createform", template,
        "grammarfield" -> grammarField,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)
  
  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {
    
    val descriptionToGrammarProblem = DescriptionToGrammarProblem.findByGeneralProblem(problem)    
    
    var shortDescription : String = problem.getShortDescription
    var longDescription : String = problem.getLongDescription
    var grammar : String = descriptionToGrammarProblem.getGrammar

    def edit(formValues : String) : JsCmd = {   
      val formValuesXml = XML.loadString(formValues)
      val grammar = (formValuesXml \ "grammarfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text
      
      val parsingErrors = GraderConnection.getGrammarParsingErrors(grammar)
      
      if(parsingErrors.isEmpty) {        
        val specificProblem : DescriptionToGrammarProblem = DescriptionToGrammarProblem.create
      
        problem.setShortDescription(shortDescription).setLongDescription(longDescription).save()
        descriptionToGrammarProblem.grammar(grammar).save()                       
        returnFunc()
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }                 
    }
     
    val grammarField = SHtml.textarea(grammar, grammar=_, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
	val shortDescriptionField = SHtml.text(shortDescription, shortDescription=_, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea(longDescription, longDescription=_, "cols" -> "80", "rows" -> "1", "id" -> "longdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val grammarFieldValXmlJs : String = "<grammarfield>' + document.getElementById('grammarfield').value + '</grammarfield>"
	val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + grammarFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), edit(_))
    
    val submit : JsCmd = hideSubmitButton & ajaxCall    
    
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={submit}>Submit</button>
    
    val template : NodeSeq = Templates(List("description-to-grammar-problem", "edit")) openOr Text("Could not find template /description-to-grammar-problem/edit")
    Helpers.bind("editform", template,
        "grammarfield" -> grammarField,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
      bestGrade: () => Int) : NodeSeq = {
    val specificProblem = DescriptionToGrammarProblem.findByGeneralProblem(generalProblem)
    
    def grade(attemptGrammar : String) : JsCmd = {
	  
	  if(remainingAttempts() <= 0) {
		return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", Text("You do not have any attempts left for this problem. Your final grade is " + bestGrade().toString + "/" + maxGrade.toString + "."))
	  }
	  val attemptTime = Calendar.getInstance.getTime()

	  val gradeAndFeedback = GraderConnection.getDescriptionToGrammarFeedback(specificProblem.grammar.is, attemptGrammar, maxGrade.toInt)
	  
	  var numericalGrade = gradeAndFeedback._1
	  val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)
	  
	  // Only save the specific attempt if we saved the general attempt and grammar was parseable
	  if(generalAttempt != null && numericalGrade >= 0) {
		DescriptionToGrammarSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptGrammar(attemptGrammar).save
	  }
	  
	  if (numericalGrade < 0) numericalGrade = 0; //parse error => no pints
	  
	  val setNumericalGrade : JsCmd = SetHtml("grade", Text(numericalGrade.toString + "/" + maxGrade.toString))
	  val setFeedback : JsCmd = SetHtml("feedback", gradeAndFeedback._2)
	  val showFeedback : JsCmd = JsShowId("feedbackdisplay")
	  
	  return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }
	
    val problemDescription = generalProblem.getLongDescription
	val grammarField = SHtml.textarea("" , value => {}, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
	
    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("document.getElementById('grammarfield').value"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
    
    val template : NodeSeq = Templates(List("description-to-grammar-problem", "solve")) openOr Text("Could not find template /description-to-grammar-problem/solve")
    Helpers.bind("solveform", template,
        "problemdescription" -> problemDescription,
		"grammarfield" -> grammarField,
        "submitbutton" -> submitButton,
        "returnlink" -> returnLink)
  }
  
  override def onDelete( generalProblem : Problem ) : Unit = {
    
  }
}