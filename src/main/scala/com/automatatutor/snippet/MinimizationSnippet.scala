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

object MinimizationSnippet extends ProblemSnippet {
  def preprocessAutomatonXml ( input : String ) : String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
                             returnFunc : () => Nothing ) : NodeSeq = {

    var shortDescription : String = ""
    var longDescription : String = ""
    var automaton : String = ""

    def create() = {
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)

      val specificProblem : MinimizationProblem = MinimizationProblem.create
      specificProblem.setGeneralProblem(unspecificProblem).setAutomaton(automaton)
      specificProblem.save

      returnFunc()
    }


    // Remember to remove all newlines from the generated XML by using filter
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _)
    val longDescriptionField = SHtml.textarea(longDescription, longDescription = _, "cols" -> "80", "rows" -> "5")
    val submitButton = SHtml.submit("Create", create, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")

    val template : NodeSeq = Templates(List("minimization", "create")) openOr Text("Could not find template /minimization/create")
    Helpers.bind("createform", template,
      "automaton" -> automatonField,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)

  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {
    // TODO : Change DFAConstructionProblem to MinimizationProblem
    val minimizationProblem = MinimizationProblem.findByGeneralProblem(problem)

    var shortDescription : String = problem.getShortDescription
    var longDescription : String = problem.getLongDescription
    var automaton : String = ""

    def create() = {
      problem.setShortDescription(shortDescription).setLongDescription(longDescription).save()
      minimizationProblem.setAutomaton(automaton).save()
      returnFunc()
    }

    // Remember to remove all newlines from the generated XML by using filter
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _)
    val longDescriptionField = SHtml.textarea(longDescription, longDescription = _, "cols" -> "80", "rows" -> "5")
    val submitButton = SHtml.submit("Edit", create, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")
    val setupScript = <script type="text/javascript"> Editor.canvas.setAutomaton("{ minimizationProblem.getAutomaton }") </script>

    val template : NodeSeq = Templates(List("minimization", "edit")) openOr Text("Could not find template /minimization/edit")
    Helpers.bind("editform", template,
      "automaton" -> automatonField,
      "setupscript" -> setupScript,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
                           recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
                           bestGrade: () => Int) : NodeSeq = {

    val minimizationProblem = MinimizationProblem.findByGeneralProblem(generalProblem)

    def grade(minimizationTableDescription : String /*, attemptDfaDescription : String*/ ) : JsCmd = {

      //Temporary:
      val attemptDfaDescription = ""

      if(remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") &
          SetHtml("feedbackdisplay",
            Text("You do not have any attempts left for this problem. Your final grade is " +
              bestGrade().toString + "/" +
              maxGrade.toString + "."))
      }

      val attemptDfaXml = XML.loadString(attemptDfaDescription)
      val correctDfaDescription = minimizationProblem.getXmlDescription.toString
      val attemptTime = Calendar.getInstance.getTime()
      val x = List(correctDfaDescription, correctDfaDescription)
      //TODO: Implement getMinimizationFeedback()
      val graderResponse = GraderConnection.getMinimizationFeedback(correctDfaDescription, minimizationTableDescription, attemptDfaDescription, maxGrade.toInt)

      val numericalGrade = graderResponse._1
      val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)

      // Only save the specific attempt if we saved the general attempt
      if(generalAttempt != null) {
        //TODO: Implement MinimizationSolutionAttempt
        MinimizationSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptAutomaton(attemptDfaDescription).save
      }

      val setNumericalGrade : JsCmd = SetHtml("grade", Text(graderResponse._1.toString + "/" + maxGrade.toString))
      val setFeedback : JsCmd = SetHtml("feedback", graderResponse._2)
      val showFeedback : JsCmd = JsShowId("feedbackdisplay")

      return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }

    val problemAlphabet = minimizationProblem.getAlphabet
    val automaton = XML.loadString(minimizationProblem.getAutomaton)
    val n = ((automaton \ "stateSet") \ "_").length
    val cyk = new Array[ Array[(Int, Int)] ] (n);
    for(i <- 0 to n - 1) {
      cyk(i) = new Array[(Int, Int)] (i+1)
      for(j <- 0 to i) {
        cyk(i)(j) = (j, i+1)
      }
    }
    val minimizationTable : NodeSeq = <table style="border-collapse: collapse;">{cyk.map(row =>
                                                  <tr>{row.map(col =>
                                                    <td style="border: 1px solid black; padding: 2px;">
                                                      <span style="white-space: nowrap;">{
                                                        SHtml.text("", value => {}, "class" -> "", "start" -> col._1.toString(), "end" -> col._2.toString(), "size" -> "5")
                                                        }{
                                                        Text("(" + col._1.toString() + "," + col._2.toString() + ")") }
                                                      </span>
                                                    </td>)}
                                                  </tr>)} </table>

    val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"
    val setupScript : NodeSeq =
      <script type="text/javascript">
        Editor.canvasDfaIn.setAutomaton( "{ minimizationProblem.getAutomaton }" )
        Editor.canvasDfaSol.setAlphabet( { alphabetJavaScriptArray } )
      </script>

    val problemAlphabetNodeSeq = Text("{" + problemAlphabet.mkString(",") + "}")
    val problemDescriptionNodeSeq = Text(generalProblem.getLongDescription)

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("buildCYKTableXML()"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))

    val template : NodeSeq = Templates(List("minimization", "solve")) openOr Text("Template /minimization/solve not found")
    return SHtml.ajaxForm(Helpers.bind("dfaeditform", template,
      "minimizationtable" -> minimizationTable,
      "setupscript" -> setupScript,
      "alphabettext" -> problemAlphabetNodeSeq,
      "problemdescription" -> problemDescriptionNodeSeq,
      "submitbutton" -> submitButton,
      "returnlink" -> returnLink))
  }

  override def onDelete( generalProblem : Problem ) : Unit = {
    MinimizationProblem.deleteByGeneralProblem(generalProblem)
  }
}
