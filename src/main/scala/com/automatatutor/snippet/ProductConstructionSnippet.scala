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
import com.automatatutor.model._
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

/**
  * Created by Jan on 21.05.2017.
  */
object ProductConstructionSnippet extends ProblemSnippet {
  def preprocessAutomatonXml ( input : String ) : String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
                             returnFunc : () => Nothing ) : NodeSeq = {

    var shortDescription : String = ""
    var longDescription : String = ""
    var booleanOperation : String = ""
    var automaton1 : String = ""
    var automaton2 : String = ""

    def create() = {
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)

      val specificProblem : ProductConstructionProblem = ProductConstructionProblem.create
      specificProblem.setGeneralProblem(unspecificProblem).setAutomaton1(automaton1)
      specificProblem.setGeneralProblem(unspecificProblem).setAutomaton2(automaton2)
      specificProblem.setGeneralProblem(unspecificProblem).setBooleanOperation(booleanOperation)
      specificProblem.save

      returnFunc()
    }

    // Remember to remove all newlines from the generated XML by using filter
    val automaton1Field = SHtml.hidden(automatonXml => automaton1 = preprocessAutomatonXml(automatonXml), "", "id" -> "automaton1Field")
    val automaton2Field = SHtml.hidden(automatonXml => automaton2 = preprocessAutomatonXml(automatonXml), "", "id" -> "automaton2Field")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _)
    val longDescriptionField = SHtml.textarea(longDescription, longDescription = _, "cols" -> "80", "rows" -> "5")
    val booleanOperationField = SHtml.text(booleanOperation, booleanOperation = _)
    //TODO:   (I) Export both automata   ;   (II) Export boolean Operation   ;   (III) Export variable amount of automata
    val submitButton = SHtml.submit("Create", create, "onClick" ->
      ("document.getElementById('automaton1Field').value = Editor.canvasDfa1.exportAutomaton();" +
      "document.getElementById('automaton2Field').value = Editor.canvasDfa2.exportAutomaton()"))

    val template : NodeSeq = Templates(List("product-construction", "create")) openOr Text("Could not find template /product-construction/create")
    Helpers.bind("createform", template,
      "automaton1" -> automaton1Field,
      "automaton2" -> automaton2Field,
      "boolop" -> booleanOperationField,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)

  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {
    // TODO : Change DFAConstructionProblem to ProductConstructionProblem
    val productConstructionProblem = ProductConstructionProblem.findByGeneralProblem(problem)

    var shortDescription : String = problem.getShortDescription
    var longDescription : String = problem.getLongDescription
    var booleanOperation : String = productConstructionProblem.getBooleanOperation.toString()
    var automaton1 : String = ""
    var automaton2 : String = ""

    def create() = {
      problem.setShortDescription(shortDescription).setLongDescription(longDescription).save()
      productConstructionProblem.setBooleanOperation(booleanOperation).save()
      productConstructionProblem.setAutomaton1(automaton1).save()
      productConstructionProblem.setAutomaton2(automaton2).save()
      returnFunc()
    }

    // Remember to remove all newlines from the generated XML by using filter
    val automaton1Field = SHtml.hidden(automatonXml => automaton1 = preprocessAutomatonXml(automatonXml), "", "id" -> "automaton1Field")
    val automaton2Field = SHtml.hidden(automatonXml => automaton2 = preprocessAutomatonXml(automatonXml), "", "id" -> "automaton2Field")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _)
    val longDescriptionField = SHtml.textarea(longDescription, longDescription = _, "cols" -> "80", "rows" -> "5")
    val booleanOperationField = SHtml.text(booleanOperation, booleanOperation = _)
    val submitButton = SHtml.submit("Edit", create, "onClick" ->
      ("document.getElementById('automaton1Field').value = Editor.canvasDfa1.exportAutomaton();" +
        "document.getElementById('automaton2Field').value = Editor.canvasDfa2.exportAutomaton()"))

    val automataList = productConstructionProblem.getAutomataList
    val setupScript =
      <script type="text/javascript">
        Editor.canvasDfa1.setAutomaton("{ automataList(0) }")
        Editor.canvasDfa2.setAutomaton("{ automataList(1) }")
      </script>

    val template : NodeSeq = Templates(List("product-construction", "edit")) openOr Text("Could not find template /product-construction/edit")
    Helpers.bind("editform", template,
      "automaton1" -> automaton1Field,
      "automaton2" -> automaton2Field,
      "setupscript" -> setupScript,
      "boolop" -> booleanOperationField,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
                           recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
                           bestGrade: () => Int) : NodeSeq = {

    val productConstructionProblem = ProductConstructionProblem.findByGeneralProblem(generalProblem)

    def grade( attemptDfaDescription : String ) : JsCmd = {
      if(remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") &
          SetHtml("feedbackdisplay",
            Text("You do not have any attempts left for this problem. Your final grade is " +
              bestGrade().toString + "/" +
              maxGrade.toString + "."))
      }

      val attemptDfaXml = XML.loadString(attemptDfaDescription)
      //TODO - Different Automata
      val correctDfaDescription1 = productConstructionProblem.getXmlDescription1.toString
      val correctDfaDescription2 = productConstructionProblem.getXmlDescription2.toString
      val booleanOperation = productConstructionProblem.getBooleanOperation.toString()
      val attemptTime = Calendar.getInstance.getTime()
      val x = List(correctDfaDescription1, correctDfaDescription2)
      val graderResponse = GraderConnection.getProductConstructionFeedback(x, attemptDfaDescription, booleanOperation, maxGrade.toInt)

      val numericalGrade = graderResponse._1
      val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)

      // Only save the specific attempt if we saved the general attempt
      if(generalAttempt != null) {
        ProductConstructionSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptAutomaton(attemptDfaDescription).save
      }

      val setNumericalGrade : JsCmd = SetHtml("grade", Text(graderResponse._1.toString + "/" + maxGrade.toString))
      val setFeedback : JsCmd = SetHtml("feedback", graderResponse._2)
      val showFeedback : JsCmd = JsShowId("feedbackdisplay")

      return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }

    val problemAlphabet = productConstructionProblem.getAlphabet
    val automataList = productConstructionProblem.getAutomataList

    val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"
    val setupScript : NodeSeq =
      <script type="text/javascript">
        Editor.canvasDfa1.setAutomaton( "{ automataList(0) }" );
        Editor.canvasDfa2.setAutomaton( "{ automataList(1) }" );
        Editor.canvasDfaSol.setAlphabet( { alphabetJavaScriptArray } )
      </script>

    val problemAlphabetNodeSeq = Text("{" + problemAlphabet.mkString(",") + "}")
    val problemDescriptionNodeSeq = Text(generalProblem.getLongDescription)
    val booleanOperationNodeSeq = Text("{" + productConstructionProblem.getBooleanOperation.toString() + "}")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("Editor.canvasDfaSol.exportAutomaton()"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))

    val template : NodeSeq = Templates(List("product-construction", "solve")) openOr Text("Template /product-construction/solve not found")
    return SHtml.ajaxForm(Helpers.bind("dfaeditform", template,
      "setupscript" -> setupScript,
      "alphabettext" -> problemAlphabetNodeSeq,
      "boolop" -> booleanOperationNodeSeq,
      "submitbutton" -> submitButton,
      "returnlink" -> returnLink))
  }

  override def onDelete( generalProblem : Problem ) : Unit = {
    ProductConstructionProblem.deleteByGeneralProblem(generalProblem)
  }
}
