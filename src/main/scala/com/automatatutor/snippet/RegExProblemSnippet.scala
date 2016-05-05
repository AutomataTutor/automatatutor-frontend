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
import com.automatatutor.model.RegExConstructionProblem
import com.automatatutor.model.RegexConstructionSolutionAttempt
import com.automatatutor.model.SolutionAttempt
import net.liftweb.common.Box
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

object RegExConstructionSnippet extends ProblemSnippet {
  def preprocessAutomatonXml ( input : String ) : String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq = {

    def create(formValues : String) : JsCmd = {
      val formValuesXml = XML.loadString(formValues)
      val regEx = (formValuesXml \ "regexfield").head.text
      val alphabet = (formValuesXml \ "alphabetfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text
      
      //Keep only the chars
      val alphabetList = alphabet.split(" ").filter(_.length()>0);
      val parsingErrors = GraderConnection.getRegexParsingErrors(regEx, alphabetList)
      
      if(parsingErrors.isEmpty) {
        val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
              
        val alphabetToSave = alphabetList.mkString(" ")
        val specificProblem : RegExConstructionProblem = RegExConstructionProblem.create
        specificProblem.problemId(unspecificProblem).regEx(regEx).alphabet(alphabetToSave)
        specificProblem.save
              
        return JsCmds.RedirectTo("/problems/index")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }
      
    }
    val alphabetField = SHtml.text("", value => {}, "id" -> "alphabetfield")
    val regExField = SHtml.text("", value => {}, "id" -> "regexfield")
    val shortDescriptionField = SHtml.text("", value => {}, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "longdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val alphabetFieldValXmlJs : String = "<alphabetfield>' + document.getElementById('alphabetfield').value + '</alphabetfield>"
    val regexFieldValXmlJs : String = "<regexfield>' + document.getElementById('regexfield').value + '</regexfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + alphabetFieldValXmlJs + regexFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), create(_))
    
    val checkAlphabetAndSubmit : JsCmd = JsIf(Call("alphabetChecks",Call("parseAlphabetByFieldName", "alphabetfield")), hideSubmitButton & ajaxCall)    
    
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={checkAlphabetAndSubmit}>Submit</button>
    
    val template : NodeSeq = Templates(List("description-to-regex-problem", "create")) openOr Text("Could not find template /description-to-regex-problem/create")
    Helpers.bind("createform", template,
        "alphabetfield" -> alphabetField,
        "regexfield" -> regExField,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Empty
  
  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
      bestGrade: () => Int) : NodeSeq = {
    val specificProblem = RegExConstructionProblem.findByGeneralProblem(generalProblem)
    
    def grade(regexAttempt : String) : JsCmd = {
      val alphabetList = specificProblem.alphabet.is.split(" ").filter(_.length()>0)
      val parsingErrors = GraderConnection.getRegexParsingErrors(regexAttempt, alphabetList)
      
      if(parsingErrors.isEmpty) {
    	if(remainingAttempts() <= 0) {
          return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", Text("You do not have any attempts left for this problem. Your final grade is " + bestGrade().toString + "/" + maxGrade.toString + "."))
        }
    	val attemptTime = Calendar.getInstance.getTime()

        val gradeAndFeedback = GraderConnection.getRegexFeedback(specificProblem.regEx.is, regexAttempt, alphabetList, maxGrade.toInt)
      
        val numericalGrade = gradeAndFeedback._1
        val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)
      
        // Only save the specific attempt if we saved the general attempt
        if(generalAttempt != null) {
    	  RegexConstructionSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptRegex(regexAttempt).save
        }
      
        val setNumericalGrade : JsCmd = SetHtml("grade", Text(gradeAndFeedback._1.toString + "/" + maxGrade.toString))
        val setFeedback : JsCmd = SetHtml("feedback", gradeAndFeedback._2)
        val showFeedback : JsCmd = JsShowId("feedbackdisplay")
      
        return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("grade", Text("")) & JsCmds.SetHtml("feedback", Text(parsingErrors.mkString("<br/>")))
      }
      
    }
    // Remember to remove all newlines from the generated XML by using filter
    val alphabetText = Text("{" + specificProblem.alphabet + "}")
    val problemDescription = generalProblem.getLongDescription
    val regExField = SHtml.text("", value => {}, "id" -> "regexfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("document.getElementById('regexfield').value"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
    
    val template : NodeSeq = Templates(List("description-to-regex-problem", "solve")) openOr Text("Could not find template /description-to-regex-problem/solve")
    Helpers.bind("regexsolveform", template,
        "alphabettext" -> alphabetText,
        "problemdescription" -> problemDescription,
        "regexattemptfield" -> regExField,
        "submitbutton" -> submitButton,
        "returnlink" -> returnLink)
  }
  
  override def onDelete( generalProblem : Problem ) : Unit = {
    
  }
}