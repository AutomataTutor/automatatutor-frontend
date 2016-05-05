package com.automatatutor.snippet

import java.util.Calendar
import java.util.Date

import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.XML

import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.NFAConstructionProblemCategory
import com.automatatutor.model.NFAToDFAProblem
import com.automatatutor.model.NFAToDFASolutionAttempt
import com.automatatutor.model.Problem
import com.automatatutor.model.SolutionAttempt

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.JsHideId
import net.liftweb.http.js.JsCmds.JsShowId
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.Templates
import net.liftweb.mapper.By
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._

object NFAToDFAProblemSnippet extends ProblemSnippet {
  def preprocessAutomatonXml ( input : String ) : String = {
    val withoutNewlines = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")
    val asXml = XML.loadString(withoutNewlines)
    val symbolsWithoutEpsilon = (asXml \ "alphabet" \ "symbol").filter(node => node.text != "ε")
    val alphabetWithoutEpsilon = <alphabet> { symbolsWithoutEpsilon } </alphabet>
    val automatonWithoutAlphabet = asXml.child.filter(_.label != "alphabet")
    val newAutomaton = <automaton> { alphabetWithoutEpsilon } { automatonWithoutAlphabet } </automaton>
    return newAutomaton.toString
  }

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq = {

    var automaton : String = ""
	var shortDescription : String = ""
    var category : NFAConstructionProblemCategory = null

    def create() = {
      val longDescription = "Construct a DFA that recognizes the same language as the given NFA"
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
      
      val specificProblem : NFAToDFAProblem = NFAToDFAProblem.create
      specificProblem.setGeneralProblem(unspecificProblem).setAutomaton(automaton)
      specificProblem.save
      
      returnFunc()
    }
    
    val allCategories = NFAConstructionProblemCategory.findAll()

    val categoryPickerEntries = allCategories.map(category => (category.id.toString, category.getCategoryName))
    def setCategoryToChosen (pickedId : String) = category = NFAConstructionProblemCategory.findByKey(pickedId.toLong) openOrThrowException("Lift has already verified that this category exists")
    
    // Remember to remove all newlines from the generated XML by using filter. Also remove 'ε' from the alphabet, as its implied
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
	val shortDescriptionField = SHtml.text("", shortDescription = _)
    val submitButton = SHtml.submit("Create", create, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")
    
    
    val template : NodeSeq = Templates(List("nfa-to-dfa-problem", "create")) openOr Text("Could not find template /nfa-to-dfa-problem/create")
    Helpers.bind("createform", template,
        "automaton" -> automatonField,
		"shortdescription" -> shortDescriptionField,		
        "submit" -> submitButton
        )
  }

  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Empty
  
  override def renderSolve ( generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt: (Int, Date)  => SolutionAttempt,
      returnFunc : () => Unit, remainingAttempts : () => Int, bestGrade : () => Int ) : NodeSeq = {
	  
    val nfaToDfaProblem = NFAToDFAProblem.findByGeneralProblem(generalProblem)
    
	def grade( attemptDfaDescription : String ) : JsCmd = {
      if(remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", 
		Text("You do not have any attempts left for this problem. Your final grade is " + 
		bestGrade().toString + "/" + maxGrade.toString + "."))
      }

      val attemptDfaXml = XML.loadString(attemptDfaDescription)
      val correctNfaDescription = nfaToDfaProblem.getXmlDescription.toString
      val attemptTime = Calendar.getInstance.getTime()
      val graderResponse = GraderConnection.getNfaToDfaFeedback(correctNfaDescription, attemptDfaDescription, maxGrade.toInt)
      
      val numericalGrade = graderResponse._1
      val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)
      
      // Only save the specific attempt if we saved the general attempt
      if (generalAttempt != null) {
    	  NFAToDFASolutionAttempt.create.solutionAttemptId(generalAttempt).attemptAutomaton(attemptDfaDescription).save
      }
      
      val setNumericalGrade : JsCmd = SetHtml("grade", Text(graderResponse._1.toString + "/" + maxGrade.toString))
      val setFeedback : JsCmd = SetHtml("feedback", graderResponse._2)
      val showFeedback : JsCmd = JsShowId("feedbackdisplay")
      
      return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }
	
	val problemAlphabet = nfaToDfaProblem.getAlphabet    
    val problemAlphabetNodeSeq = Text("{" + problemAlphabet.mkString(",") + "}")
    val problemDescriptionNodeSeq = Text(generalProblem.getLongDescription)
    
    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("Editor.canvasDfa.exportAutomaton()"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    
	val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
	val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"
    val setupScript : NodeSeq =
      <script type="text/javascript">
		Editor.canvasNfa.setAutomaton( '{ nfaToDfaProblem.getXmlDescription.toString }' );
    	Editor.canvasDfa.setAlphabet( { alphabetJavaScriptArray } );
      </script>
	
    	
    	
    val template : NodeSeq = Templates(List("nfa-to-dfa-problem", "solve")) openOr Text("Template /nfa-to-dfa-problem/solve not found")
    return SHtml.ajaxForm(Helpers.bind("nfatodfaform", template,
	    "setupscript" -> setupScript,
	    "returnlink" -> returnLink,
        "submitbutton" -> submitButton))
	
  }
    
  override def onDelete( problem : Problem ) : Unit = {
    NFAToDFAProblem.deleteByGeneralProblem(problem)
  }
}