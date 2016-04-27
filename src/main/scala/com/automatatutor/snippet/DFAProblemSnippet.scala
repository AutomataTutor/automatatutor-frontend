package com.automatatutor.snippet

import java.util.Calendar
import java.util.Date

import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Text
import scala.xml.XML

import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.DFAConstructionProblem
import com.automatatutor.model.DFAConstructionProblemCategory
import com.automatatutor.model.DFAConstructionSolutionAttempt
import com.automatatutor.model.Problem
import com.automatatutor.model.SolutionAttempt

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.Templates
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.JsHideId
import net.liftweb.http.js.JsCmds.JsShowId
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.mapper.By
import net.liftweb.util.AnyVar.whatVarIs
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.strToSuperArrowAssoc

object DFAConstructionSnippet extends ProblemSnippet {
  def preprocessAutomatonXml ( input : String ) : String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
      returnFunc : () => Nothing ) : NodeSeq = {

    var shortDescription : String = ""
    var longDescription : String = ""
    var automaton : String = ""
    var category : DFAConstructionProblemCategory = null

    def create() = {
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
      
      val specificProblem : DFAConstructionProblem = DFAConstructionProblem.create
      specificProblem.setGeneralProblem(unspecificProblem).setCategory(category).setAutomaton(automaton)
      specificProblem.save
      
      returnFunc()
    }
    
    val allCategories = DFAConstructionProblemCategory.findAll()

    val categoryPickerEntries = allCategories.map(category => (category.id.toString, category.getCategoryName))
    def setCategoryToChosen (pickedId : String) = category = DFAConstructionProblemCategory.findByKey(pickedId.toLong) openOrThrowException("Lift has already verified that this category exists")
    
    // Remember to remove all newlines from the generated XML by using filter
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val categoryPicker = SHtml.select(categoryPickerEntries, Empty, setCategoryToChosen)
    val shortDescriptionField = SHtml.text("", shortDescription = _)
    val longDescriptionField = SHtml.textarea("", longDescription = _, "cols" -> "80", "rows" -> "5")
    val submitButton = SHtml.submit("Create", create, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")
    
    val template : NodeSeq = Templates(List("dfa-construction", "create")) openOr Text("Could not find template /dfa-construction/create")
    Helpers.bind("createform", template,
        "automaton" -> automatonField,
        "category" -> categoryPicker,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
  
  override def renderEdit( generalProblem : Problem ) : NodeSeq = {
        
    return null;
    /*val dfaConstructionProblem : DFAConstructionProblem = DFAConstructionProblem.findByGeneralProblem(generalProblem)
    val automatonXml : String = dfaConstructionProblem.automaton.is
    val setAutomatonXmlJs : NodeSeq = <script type='text/javascript'> var automatonXml = " { automatonXml } " </script>
    val applet : NodeSeq = <lift:embed what="/dfa-construction/edit" />

    var shortDescription : String = ""
    var longDescription : String = ""
    var automaton : String = ""
    var category : DFAConstructionProblemCategory = null

    def edit() = {
      val unspecificProblem = editUnspecificProb(shortDescription, longDescription)
      
      val specificProblem : DFAConstructionProblem = DFAConstructionProblem.create
      specificProblem.problemId(unspecificProblem).category(category).automaton(automaton)
      specificProblem.save
      
      returnFunc()
    }
    
    val allCategories = DFAConstructionProblemCategory.findAll()

    val categoryPickerEntries = allCategories.map(category => (category.id.toString, category.categoryName.is))
    def setCategoryToChosen (pickedId : String) = category = DFAConstructionProblemCategory.findByKey(pickedId.toLong) openOrThrowException("Lift has already verified that this category exists")
    
    // Remember to remove all newlines from the generated XML by using filter
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val categoryPicker = SHtml.select(categoryPickerEntries, Empty, setCategoryToChosen)
    val shortDescriptionField = SHtml.text("", shortDescription = _)
    val longDescriptionField = SHtml.textarea("", longDescription = _, "cols" -> "80", "rows" -> "5")
    val submitButton = SHtml.submit("Edit", edit, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")
    
    val problemAlphabet = dfaConstructionProblem.getAlphabet 
    val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"
    val setupScript : NodeSeq =
      <script type="text/javascript">
    Editor.canvasDfa.setAutomaton( '{ dfaConstructionProblem.getXmlDescription.toString }' );
      Editor.canvasDfa.setAlphabet( { alphabetJavaScriptArray } );
      </script>
    
    val template : NodeSeq = Templates(List("dfa-construction", "create")) openOr Text("Could not find template /dfa-construction/create")
    Helpers.bind("editform", template,
        "automaton" -> automatonField,
        "category" -> categoryPicker,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)*/
  }
  
  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
      recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
      bestGrade: () => Int) : NodeSeq = {
	  
    val dfaConstructionProblem = DFAConstructionProblem.findByGeneralProblem(generalProblem)
    
    def grade( attemptDfaDescription : String ) : JsCmd = {
      if(remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") & 
		SetHtml("feedbackdisplay", 
		Text("You do not have any attempts left for this problem. Your final grade is " + 
				bestGrade().toString + "/" + 
				maxGrade.toString + "."))
      }

      val attemptDfaXml = XML.loadString(attemptDfaDescription)
      val correctDfaDescription = dfaConstructionProblem.getXmlDescription.toString
      val attemptTime = Calendar.getInstance.getTime()
      val graderResponse = GraderConnection.getDfaFeedback(correctDfaDescription, attemptDfaDescription, maxGrade.toInt)
      
      val numericalGrade = graderResponse._1
      val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)
      
      // Only save the specific attempt if we saved the general attempt
      if(generalAttempt != null) {
    	  DFAConstructionSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptAutomaton(attemptDfaDescription).save
      }
      
      val setNumericalGrade : JsCmd = SetHtml("grade", Text(graderResponse._1.toString + "/" + maxGrade.toString))
      val setFeedback : JsCmd = SetHtml("feedback", graderResponse._2)
      val showFeedback : JsCmd = JsShowId("feedbackdisplay")
      
      return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }
    
    val problemAlphabet = dfaConstructionProblem.getAlphabet
    
    val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"
    val alphabetScript : NodeSeq = <script type="text/javascript"> Editor.canvas.setAlphabet( { alphabetJavaScriptArray } ) </script>
    
    val problemAlphabetNodeSeq = Text("{" + problemAlphabet.mkString(",") + "}")
    val problemDescriptionNodeSeq = Text(generalProblem.longDescription.is)
    
    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("Editor.canvas.exportAutomaton()"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
    
    val template : NodeSeq = Templates(List("dfa-construction", "solve")) openOr Text("Template /dfa-construction/solve not found")
    return SHtml.ajaxForm(Helpers.bind("dfaeditform", template,
        "alphabetscript" -> alphabetScript,
        "alphabettext" -> problemAlphabetNodeSeq,
        "problemdescription" -> problemDescriptionNodeSeq,
        "submitbutton" -> submitButton,
        "returnlink" -> returnLink))
  }
  
  override def onDelete( generalProblem : Problem ) : Unit = {
    DFAConstructionProblem.deleteByGeneralProblem(generalProblem)
  }
}

// We have this as an extra class in order to get around Lift's problems with proper capitalization when looking for snippets
class Dfacreationsnippet {
  def preprocessAutomatonXml ( input : String ) : String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")
  
  def editform( xhtml : NodeSeq ) : NodeSeq = {
    val unspecificProblem : Problem = chosenProblem
    val dfaConstructionProblem : DFAConstructionProblem = DFAConstructionProblem.findByGeneralProblem(chosenProblem)

    var shortDescription : String = chosenProblem.shortDescription.is
    var longDescription : String = chosenProblem.longDescription.is
    var automaton : String = "" // Will get replaced by an XML-description of the canvas anyways
    var category = dfaConstructionProblem.getCategory

    def edit() = {
      unspecificProblem.shortDescription(shortDescription).longDescription(longDescription).save
      dfaConstructionProblem.setAutomaton(automaton).save
      
      S.redirectTo("/problems/index")
    }
    
    val allCategories = DFAConstructionProblemCategory.findAll()
    
    val categoryPickerEntries = allCategories.map(category => (category.id.toString, category.getCategoryName))
    def setCategoryToChosen (pickedId : String) = category = DFAConstructionProblemCategory.findByKey(pickedId.toLong) openOrThrowException("Lift has already verified that this category exists")
    
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val categoryPicker = SHtml.select(categoryPickerEntries, Full(category.id.toString), setCategoryToChosen)
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _)
    val longDescriptionField = SHtml.textarea(longDescription, longDescription = _, "cols" -> "80", "rows" -> "5")
    val submitButton = SHtml.submit("Submit", edit, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")
    
    Helpers.bind("createform", xhtml,
        "automaton" -> automatonField,
        "category" -> categoryPicker,
        "shortdescription" -> shortDescriptionField,
        "longdescription" -> longDescriptionField,
        "submit" -> submitButton)
  }
}