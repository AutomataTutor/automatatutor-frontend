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
import com.automatatutor.model.CYKProblem
import com.automatatutor.model.CYKSolutionAttempt
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

object CYKProblemSnippet extends ProblemSnippet {

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq = {

    def create(formValues : String) : JsCmd = {
      val formValuesXml = XML.loadString(formValues)
      val grammar = (formValuesXml \ "grammarfield").head.text
      val word = (formValuesXml \ "wordfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text
      
      val parsingErrors = GraderConnection.getCNFParsingErrors(grammar)
      
      if(parsingErrors.isEmpty) {
        val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
		
        val specificProblem : CYKProblem = CYKProblem.create
        specificProblem.problemId(unspecificProblem).grammar(grammar).word(word)
        specificProblem.save
              
        return JsCmds.RedirectTo("/problems/index")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }
      
    }
    val grammarField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
    val wordField = SHtml.text("", value => {}, "id" -> "wordfield")
    val shortDescriptionField = SHtml.text("", value => {}, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "longdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val grammarFieldValXmlJs : String = "<grammarfield>' + document.getElementById('grammarfield').value + '</grammarfield>"
    val wordFieldValXmlJs : String = "<wordfield>' + document.getElementById('wordfield').value + '</wordfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + grammarFieldValXmlJs + wordFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), create(_))
    
    val submit : JsCmd = hideSubmitButton & ajaxCall
    
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={submit}>Submit</button>
    
    val template : NodeSeq = Templates(List("cyk-problem", "create")) openOr Text("Could not find template /cyk-problem/create")
    Helpers.bind("createform", template,
        "grammarfield" -> grammarField,
        "wordfield" -> wordField,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)
  
  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {
    
    val cykProblem = CYKProblem.findByGeneralProblem(problem)    
    
    var shortDescription : String = problem.getShortDescription
    var longDescription : String = problem.getLongDescription
    var grammar : String = cykProblem.getGrammar
    var word : String = cykProblem.getWord

    def edit(formValues : String) : JsCmd = {   
      val formValuesXml = XML.loadString(formValues)
      val grammar = (formValuesXml \ "grammarfield").head.text
      val word = (formValuesXml \ "wordfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text
      
      val parsingErrors = GraderConnection.getCNFParsingErrors(grammar)
      
      if(parsingErrors.isEmpty) {        
        val specificProblem : CYKProblem = CYKProblem.create
      
        problem.setShortDescription(shortDescription).setLongDescription(longDescription).save()
        cykProblem.grammar(grammar).word(word).save()                       
        returnFunc()
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }                 
    }
     
    val grammarField = SHtml.textarea(grammar, grammar=_, "cols" -> "80", "rows" -> "5", "id" -> "grammarfield")
    val wordField = SHtml.text(word, word=_, "id" -> "wordfield")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription=_, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea(longDescription, longDescription=_, "cols" -> "80", "rows" -> "1", "id" -> "longdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val grammarFieldValXmlJs : String = "<grammarfield>' + document.getElementById('grammarfield').value + '</grammarfield>"
    val wordFieldValXmlJs : String = "<wordfield>' + document.getElementById('wordfield').value + '</wordfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + grammarFieldValXmlJs + wordFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), edit(_))
    
    val submit : JsCmd = hideSubmitButton & ajaxCall    
    
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={submit}>Submit</button>
    
    val template : NodeSeq = Templates(List("cyk-problem", "edit")) openOr Text("Could not find template /cyk-problem/edit")
    Helpers.bind("editform", template,
        "grammarfield" -> grammarField,
        "wordfield" -> wordField,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
      bestGrade: () => Int) : NodeSeq = {
    val specificProblem = CYKProblem.findByGeneralProblem(generalProblem)
    
    def grade(cyk_table : String) : JsCmd = {
	  if(remainingAttempts() <= 0) {
		return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", Text("You do not have any attempts left for this problem. Your final grade is " + bestGrade().toString + "/" + maxGrade.toString + "."))
	  }
	  val attemptTime = Calendar.getInstance.getTime()

	  val gradeAndFeedback = GraderConnection.getCYKFeedback(specificProblem.grammar.is, specificProblem.word.is, cyk_table, maxGrade.toInt)
	  
	  val numericalGrade = gradeAndFeedback._1
	  val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)
	  
	  // Only save the specific attempt if we saved the general attempt
	  if(generalAttempt != null) {
		CYKSolutionAttempt.create.solutionAttemptId(generalAttempt).attempt(cyk_table).save
	  }
	  
	  val setNumericalGrade : JsCmd = SetHtml("grade", Text(gradeAndFeedback._1.toString + "/" + maxGrade.toString))
	  val setFeedback : JsCmd = SetHtml("feedback", gradeAndFeedback._2)
	  val showFeedback : JsCmd = JsShowId("feedbackdisplay")
	  
	  return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }
	
    val problemDescription = generalProblem.getLongDescription
	val grammarText = { specificProblem.getGrammar.replaceAll("->", " -> ").replaceAll("=>", " -> ").replaceAll("\\|", " \\| ").replaceAll("\\s{2,}", " ").split("\\s(?=\\S+\\s*->)").map {Text(_) ++ <br/> } reduceLeft (_ ++ _) }
	val wordText = Text("" + specificProblem.word)
	
	val word = specificProblem.getWord
	val n = word.length()
	val cyk = new Array[ Array[(Int, Int)] ] (n);
	for(i <- 0 to n - 1) {
		cyk(i) = new Array[(Int, Int)] (i+1)
		for(j <- 0 to i) {
			cyk(i)(j) = (j+1, n + j - i) 
		}
	}
	val cykTable = <table style="border-collapse: collapse;">{cyk.map(row => <tr>{row.map(col => <td style="border: 1px solid black; padding: 2px;"><span style="white-space: nowrap;">{ SHtml.text("", value => {}, "class" -> "cyk", "start" -> col._1.toString(), "end" -> col._2.toString(), "size" -> "12") }{ Text("(" + col._1.toString() + "," + col._2.toString() + ")") }</span></td>)}</tr>)} <tr>{ word.map(c => <td style="text-align: center">{"'" + c.toString + "'"}</td>) }</tr></table>
	
    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("buildCYKTableXML()"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
    
    val template : NodeSeq = Templates(List("cyk-problem", "solve")) openOr Text("Could not find template /cyk-problem/solve")
    Helpers.bind("solveform", template,
        "problemdescription" -> problemDescription,
		"grammartext" -> grammarText,
		"wordtext" -> wordText,
		"cyktable" -> cykTable,
        "submitbutton" -> submitButton,
        "returnlink" -> returnLink)
  }
  
  override def onDelete( generalProblem : Problem ) : Unit = {
    
  }
}