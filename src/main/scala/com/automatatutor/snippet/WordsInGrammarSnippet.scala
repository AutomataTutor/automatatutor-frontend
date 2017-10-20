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
import com.automatatutor.model.WordsInGrammarProblem
import com.automatatutor.model.WordsInGrammarSolutionAttempt
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

object WordsInGrammarSnippet extends ProblemSnippet {

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq = {

    def create(formValues : String) : JsCmd = {
      val formValuesXml = XML.loadString(formValues)
      val grammar = (formValuesXml \ "grammarfield").head.text
	  val inNeeded = (formValuesXml \ "inneededfield").head.text.toInt
	  val outNeeded = (formValuesXml \ "outneededfield").head.text.toInt
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      
      val parsingErrors = GraderConnection.getGrammarParsingErrors(grammar)
      
      if(parsingErrors.isEmpty) {
        val unspecificProblem = createUnspecificProb(shortDescription, shortDescription)
		
        val specificProblem : WordsInGrammarProblem = WordsInGrammarProblem.create
        specificProblem.problemId(unspecificProblem).grammar(grammar).inNeeded(inNeeded).outNeeded(outNeeded)
        specificProblem.save
              
        return JsCmds.RedirectTo("/problems/index")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }
      
    }
    val grammarField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
	val inNeededField = SHtml.select( Array(("1","1"), ("2","2"), ("3","3"), ("4","4"), ("5","5")), Empty , value => {}, "id" -> "inneededfield")
	val outNeededField = SHtml.select( Array(("1","1"), ("2","2"), ("3","3"), ("4","4"), ("5","5")), Empty , value => {}, "id" -> "outneededfield")
    val shortDescriptionField = SHtml.text("", value => {}, "id" -> "shortdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val grammarFieldValXmlJs : String = "<grammarfield>' + document.getElementById('grammarfield').value + '</grammarfield>"
	val inNeededFieldValXmlJs : String = "<inneededfield>' + document.getElementById('inneededfield').value + '</inneededfield>"
	val outNeededFieldValXmlJs : String = "<outneededfield>' + document.getElementById('outneededfield').value + '</outneededfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + grammarFieldValXmlJs + inNeededFieldValXmlJs + outNeededFieldValXmlJs + shortdescFieldValXmlJs + "</createattempt>'"), create(_))
    
    //val checkGrammarAndSubmit : JsCmd = JsIf(Call("multipleAlphabetChecks",Call("parseAlphabetByFieldName", "terminalsfield"),Call("parseAlphabetByFieldName", "nonterminalsfield")), hideSubmitButton & ajaxCall)    
	val submit : JsCmd = hideSubmitButton & ajaxCall
    
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={submit}>Submit</button>
    
    val template : NodeSeq = Templates(List("words-in-grammar-problem", "create")) openOr Text("Could not find template /words-in-grammar-problem/create")
    Helpers.bind("createform", template,
        "grammarfield" -> grammarField,
        "inneededfield" -> inNeededField,
        "outneededfield" -> outNeededField,
        "shortdescription" -> shortDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)
  
  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {
    
    val wordsInGrammarProblem = WordsInGrammarProblem.findByGeneralProblem(problem)    
    
    var shortDescription : String = problem.getShortDescription
    var grammar : String = wordsInGrammarProblem.getGrammar
    var inNeeded : Int = wordsInGrammarProblem.getInNeeded
    var outNeeded : Int = wordsInGrammarProblem.getOutNeeded

    def edit(formValues : String) : JsCmd = {   
      val formValuesXml = XML.loadString(formValues)
      val grammar = (formValuesXml \ "grammarfield").head.text
	  val inNeeded = (formValuesXml \ "inneededfield").head.text.toInt
	  val outNeeded = (formValuesXml \ "outneededfield").head.text.toInt
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      
      val parsingErrors = GraderConnection.getGrammarParsingErrors(grammar)
      
      if(parsingErrors.isEmpty) {        
        val specificProblem : WordsInGrammarProblem = WordsInGrammarProblem.create
      
        problem.setShortDescription(shortDescription).setLongDescription(shortDescription).save()
        wordsInGrammarProblem.grammar(grammar).inNeeded(inNeeded).outNeeded(outNeeded).save()                       
        returnFunc()
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }                 
    }
     
    val grammarField = SHtml.textarea(grammar, grammar=_, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
	val inNeededField = SHtml.select( Array(("1","1"), ("2","2"), ("3","3"), ("4","4"), ("5","5")), Full("" + inNeeded) , value => {}, "id" -> "inneededfield")
	val outNeededField = SHtml.select( Array(("1","1"), ("2","2"), ("3","3"), ("4","4"), ("5","5")), Full("" + outNeeded) , value => {}, "id" -> "outneededfield")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription=_, "id" -> "shortdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val grammarFieldValXmlJs : String = "<grammarfield>' + document.getElementById('grammarfield').value + '</grammarfield>"
	val inNeededFieldValXmlJs : String = "<inneededfield>' + document.getElementById('inneededfield').value + '</inneededfield>"
	val outNeededFieldValXmlJs : String = "<outneededfield>' + document.getElementById('outneededfield').value + '</outneededfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + grammarFieldValXmlJs + inNeededFieldValXmlJs + outNeededFieldValXmlJs + shortdescFieldValXmlJs + "</createattempt>'"), edit(_))
    
    val submit : JsCmd = hideSubmitButton & ajaxCall    
    
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={submit}>Submit</button>
    
    val template : NodeSeq = Templates(List("words-in-grammar-problem", "edit")) openOr Text("Could not find template /words-in-grammar-problem/edit")
    Helpers.bind("editform", template,
        "grammarfield" -> grammarField,
        "inneededfield" -> inNeededField,
        "outneededfield" -> outNeededField,
        "shortdescription" -> shortDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
      bestGrade: () => Int) : NodeSeq = {
    val specificProblem = WordsInGrammarProblem.findByGeneralProblem(generalProblem)
    
    def grade(formValues : String) : JsCmd = {
	  val formValuesXml = XML.loadString(formValues)
	  
	  val wordsIn = new Array[String](specificProblem.getInNeeded);
	  for(i <- 0 to specificProblem.getInNeeded - 1) {
		wordsIn(i) = (formValuesXml \ "ins" \ ("in" + i)).head.text.replaceAll("\\s", "")
	  }
	  val wordsOut = new Array[String](specificProblem.getOutNeeded);
	  for(i <- 0 to specificProblem.getOutNeeded - 1) {
		wordsOut(i) = (formValuesXml \ "outs" \ ("out" + i)).head.text.replaceAll("\\s", "")
	  }
	  
	  if(remainingAttempts() <= 0) {
		return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", Text("You do not have any attempts left for this problem. Your final grade is " + bestGrade().toString + "/" + maxGrade.toString + "."))
	  }
	  val attemptTime = Calendar.getInstance.getTime()

	  val gradeAndFeedback = GraderConnection.getWordsInGrammarFeedback(specificProblem.grammar.is, wordsIn, wordsOut, maxGrade.toInt)
	  
	  val numericalGrade = gradeAndFeedback._1
	  val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)
	  
	  // Only save the specific attempt if we saved the general attempt
	  if(generalAttempt != null) {
		WordsInGrammarSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptWordsIn((formValuesXml \ "ins").toString()).attemptWordsOut((formValuesXml \ "outs").toString()).save
	  }
	  
	  val setNumericalGrade : JsCmd = SetHtml("grade", Text(gradeAndFeedback._1.toString + "/" + maxGrade.toString))
	  val setFeedback : JsCmd = SetHtml("feedback", gradeAndFeedback._2)
	  val showFeedback : JsCmd = JsShowId("feedbackdisplay")
	  
	  return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }
	
    val problemDescription = generalProblem.getLongDescription
	val grammarText = { specificProblem.getGrammar.replaceAll("->", " -> ").replaceAll("=>", " -> ").replaceAll("\\|", " \\| ").replaceAll("\\s{2,}", " ").split("\\s(?=\\S+\\s*->)").map {Text(_) ++ <br/> } reduceLeft (_ ++ _) }
	var inNeededText = Text(specificProblem.inNeeded + " words")
	if (specificProblem.inNeeded == 1) inNeededText = Text(specificProblem.inNeeded + " word")
	var outNeededText = Text(specificProblem.outNeeded + " words")
	if (specificProblem.outNeeded == 1) outNeededText = Text(specificProblem.outNeeded + " word")
	val wordsInFields = new Array[NodeSeq](specificProblem.getInNeeded)
	for(i <- 0 to specificProblem.getInNeeded - 1) {
		wordsInFields(i) = SHtml.text("", value => {}, "id" -> ("wordinfield" + i.toString), "maxlength" -> "75")
	}
	val wordsInFieldNodeSeq = <ul>{wordsInFields.map(i => <li>{i}</li>)}</ul>
	val wordsOutFields = new Array[NodeSeq](specificProblem.getOutNeeded)
	for(i <- 0 to specificProblem.getOutNeeded - 1) {
		wordsOutFields(i) = SHtml.text("", value => {}, "id" -> ("wordoutfield" + i.toString), "maxlength" -> "75")
	}
	val wordsOutFieldNodeSeq = <ul>{wordsOutFields.map(i => <li>{i}</li>)}</ul>
	
	val insValXmlJs : StringBuilder = new StringBuilder("<ins>")
	for(i <- 0 to specificProblem.getInNeeded - 1) {
		insValXmlJs.append("<in" + i.toString + ">' + sanitizeInputForXML('wordinfield" + i.toString + "') + '</in" + i.toString + ">")
	}
	insValXmlJs.append("</ins>")
	val outsValXmlJs : StringBuilder = new StringBuilder("<outs>")
	for(i <- 0 to specificProblem.getOutNeeded - 1) {
		outsValXmlJs.append("<out" + i.toString + ">' + sanitizeInputForXML('wordoutfield" + i.toString + "') + '</out" + i.toString + ">")
	}
	outsValXmlJs.append("</outs>")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<solveattempt>" + insValXmlJs + outsValXmlJs + "</solveattempt>'"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
    
    val template : NodeSeq = Templates(List("words-in-grammar-problem", "solve")) openOr Text("Could not find template /words-in-grammar-problem/solve")
    Helpers.bind("solveform", template,
        "problemdescription" -> problemDescription,
		"grammartext" -> grammarText,
		"wordsin" -> wordsInFieldNodeSeq,
		"wordsout" -> wordsOutFieldNodeSeq,
		"inneededtext" -> inNeededText,
		"outneededtext" -> outNeededText,
        "submitbutton" -> submitButton,
        "returnlink" -> returnLink)
  }
  
  override def onDelete( generalProblem : Problem ) : Unit = {
    
  }
}