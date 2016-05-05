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
import com.automatatutor.model.PumpingLemmaProblem
import com.automatatutor.model.PumpingLemmaSolutionAttempt
import com.automatatutor.model.SolutionAttempt
import net.liftweb.common.Box
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.Templates
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmd._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.JsIf
import net.liftweb.http.js.JsCmds.JsHideId
import net.liftweb.http.js.JsCmds.JsShowId
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.SHtmlJ
import net.liftweb.util.Html5
import net.liftweb.common.Empty

object PumpingLemmaProblemSnippet extends ProblemSnippet {
  def preprocessAutomatonXml(input: String): String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")

  override def renderCreate(createUnspecificProb: (String, String) => Problem,
                            returnFunc: () => Nothing): NodeSeq = {

    def create(formValues: String): JsCmd = {

      val formValuesXml = XML.loadString(formValues)
      val language = (formValuesXml \ "languagefield").head.text
      val constraint = (formValuesXml \ "constraintfield").head.text
      val alphabet = (formValuesXml \ "alphabetfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text
      val pumpingString = (formValuesXml \ "pumpingstrfield").head.text

      val alphabetList = alphabet.split(" ").filter(_.length() > 0)

      val parsingErrors = GraderConnection.getPLParsingErrors(language, constraint, alphabetList, pumpingString)

      if (parsingErrors.isEmpty) {

        val unspecificProblem = createUnspecificProb(shortDescription, longDescription)
        val alphabetToSave = alphabetList.mkString(" ")
        val specificProblem: PumpingLemmaProblem = PumpingLemmaProblem.create

        specificProblem
          .problemId(unspecificProblem)
          .language(language)
          .constraint(constraint)
          .alphabet(alphabetToSave)
          .pumpingString(pumpingString)

        specificProblem.save

        // TODO need to see if solvable first and add pumping string
        //specificProblem.pumpingString("aa")   

        return JsCmds.RedirectTo("/problems/index")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") &
          JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }

    }

    val alphabetField = SHtml.text("", value => { "a b" }, "id" -> "alphabetfield")
    val languageField = SHtml.text("", value => { "a^n b^m" }, "id" -> "languagefield")
    val constraintField = SHtml.text("", value => { "m=n" }, "id" -> "constraintfield")
    val pumpingStrField = SHtml.text("", value => { "a^p b^p" }, "id" -> "pumpingstrfield")
    val shortDescriptionField = SHtml.text("", value => { "Same number of as and bs and a* b*" }, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea("", value => { "Same number of as and bs and a* b*" }, "cols" -> "80", "rows" -> "5", "id" -> "longdescfield")

    val hideSubmitButton: JsCmd = JsHideId("submitbutton")
    val alphabetFieldValXmlJs: String = "<alphabetfield>' + document.getElementById('alphabetfield').value + '</alphabetfield>"
    val languageFieldValXmlJs: String = "<languagefield>' + document.getElementById('languagefield').value + '</languagefield>"
    val constraintFieldValXmlJs: String = "<constraintfield>' + document.getElementById('constraintfield').value + '</constraintfield>"
    val shortdescFieldValXmlJs: String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs: String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val pumpingstrFieldValXmlJs: String = "<pumpingstrfield>' + document.getElementById('pumpingstrfield').value + '</pumpingstrfield>"
    val ajaxCall: JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + alphabetFieldValXmlJs + languageFieldValXmlJs + constraintFieldValXmlJs + pumpingstrFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), create(_))

    val checkAlphabetAndSubmit: JsCmd = JsIf(Call("alphabetChecks", Call("parseAlphabetByFieldName", "alphabetfield")), hideSubmitButton & ajaxCall)

    val submitButton: NodeSeq =
      <button type='button' id='submitbutton' onclick={ checkAlphabetAndSubmit }>Submit</button>

    val template: NodeSeq = Templates(List("pumping-lemma-problem", "create")) openOr Text("Could not find template /description-to-regex-problem/create")
    Helpers.bind("createform", template,
      "alphabetfield" -> alphabetField,
      "languagefield" -> languageField,
      "constraintfield" -> constraintField,
      "pumpingstrfield" -> pumpingStrField,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Empty

  override def renderSolve(generalProblem: Problem, maxGrade: Long, lastAttempt: Box[SolutionAttempt],
                           recordSolutionAttempt: (Int, Date) => SolutionAttempt, returnFunc: () => Unit, remainingAttempts: () => Int,
                           bestGrade: () => Int): NodeSeq = {
    val specificProblem = PumpingLemmaProblem.findByGeneralProblem(generalProblem)
    val alphabetList = specificProblem.alphabet.is.split(" ")

    val alphabetText = Text("{" + specificProblem.alphabet + "}")
    val language = specificProblem.language.is
    val constraint = specificProblem.constraint.is

    val template: NodeSeq = Templates(List("pumping-lemma-problem", "solve")) openOr
      Text("Could not find template /pumping-lemma-problem/solve")

    def submit(formValues: String): JsCmd = {

      if (remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", Text("You do not have any attempts left for this problem. Your final grade is " + bestGrade().toString + "/" + maxGrade.toString + "."))
      }
      val attemptTime = Calendar.getInstance.getTime()

      val formValuesXml = XML.loadString(formValues);
      val pumpingString = (formValuesXml \ "pumpingstring").head.text
      val pumpingNumbers = (formValuesXml \ "pumps").head

      val errorsOrSplits = GraderConnection.getPLFeedback(
        language,
        constraint,
        alphabetList,
        pumpingString,
        pumpingNumbers)
      (
        specificProblem.language.is, specificProblem.constraint.is,
        alphabetList,
        pumpingString)

      val root = errorsOrSplits.head;

      root.label match {
        case "result" =>
          {                       
            val numericalGrade = (root \ "grade").head.text.toInt
            val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)

            val feedback = <div> {(root \ "feedback").head.text} </div>
            
            // Only save the specific attempt if we saved the general attempt
            if (generalAttempt != null) {
              PumpingLemmaSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptPL(formValues).save
            }
            
            val setNumericalGrade: JsCmd = SetHtml("grade", Text(numericalGrade.toString + "/" + maxGrade.toString))
            val setFeedback: JsCmd = SetHtml("feedback", feedback)
            val showFeedback: JsCmd = JsShowId("feedbackdisplay")

            return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")                        
          }
        case "error" =>
          return JsCmds.JsShowId("submitButton") &
            JsCmds.JsShowId("feedbackdisplay") &
            JsCmds.SetHtml("grade", Text("")) &
            JsCmds.SetHtml("feedback", Text(root.text))
        case _ =>
          return JsShowId("feedbackdisplay")
      }
    }

    def extractSplits(pumpingString: String): JsCmd = {

      if (remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay", Text("You do not have any attempts left for this problem. Your final grade is " + bestGrade().toString + "/" + maxGrade.toString + "."))
      }
      val attemptTime = Calendar.getInstance.getTime()

      val errorsOrSplits = GraderConnection.getPLSplits(
        specificProblem.language.is, specificProblem.constraint.is,
        alphabetList,
        pumpingString)

      val root = errorsOrSplits.head;

      root.label match {
        case "symbstrings" =>
          {
            var command = JsCmds.JsHideId("generateSplitsButton") & JsCmds.JsShowId("splitdisplay")
            var stringId = 0
            //Creates the form elements to submit the pumping sizes

            var formElems: String = "";
            //Creates the divs for the svg images of the splits
            var newDivs = new xml.NodeBuffer
            for (symbString <- root.child) {
              newDivs += <div id={ "symbStr" + stringId }></div>
              val textID = "symbStr" + stringId + "text";
              newDivs += <span>{ "By how much should you pump y? (e.g., 0, 2, p): " }</span>
              newDivs += SHtml.text("", value => {}, "id" -> { textID })
              formElems += "<pump>' + document.getElementById('" + textID + "').value + '</pump>";
              newDivs += <br/>
              stringId = stringId + 1
            }
            formElems = "<pumpingstring>" + pumpingString + "</pumpingstring><pumps>" + formElems + "</pumps>";

            //Create button for submission
            val ajaxSubmit: JsCmd = SHtml.ajaxCall(JsRaw("'<plattempt>" + formElems + "</plattempt>'"), submit(_))
            val hideSubmitButton: JsCmd = JsHideId("submitbutton")
            def submitForm() { hideSubmitButton & ajaxSubmit }
            newDivs += <button type='button' id='submitButton' onclick={ hideSubmitButton & ajaxSubmit }>
                         Submit
                       </button>

            command = command & JsCmds.SetHtml("splitsdiv", newDivs)

            //Initialize canvas
            stringId = 0
            for (symbString <- root.child) {
              command = command & JsCmds.Run("initCanvas('symbStr" + stringId + "','" + symbString + "');")
              stringId = stringId + 1
            }

            return command;
          }
        case "error" =>
          return JsCmds.JsShowId("generateSplitsButton") &
            JsCmds.JsShowId("feedbackdisplay") &
            JsCmds.SetHtml("grade", Text("")) &
            JsCmds.SetHtml("feedback", Text(root.text))
        case _ =>
          return JsShowId("feedbackdisplay")
      }

    }

    //Functions for async call and hide button
    val hideGenerateSplitsButton: JsCmd = JsHideId("generateSplitsButton")
    val ajaxGenerateSplits: JsCmd = SHtml.ajaxCall(JsRaw("document.getElementById('pumpingstringfield').value"), extractSplits(_))
    //val ajaxSubmit : JsCmd = SHtml.ajaxCall(JsRaw("document.getElementById('pumpingsstringfield').value"), extractSplits(_))
    // Remember to remove all newlines from the generated XML by using filter    

    val pumpingStField = SHtml.text("", value => {}, "id" -> "pumpingstringfield")

    val generateSplitsButton: NodeSeq = <button type='button' id='generateSplitsButton' onclick={ hideGenerateSplitsButton & ajaxGenerateSplits }>
                                          Generate string splits
                                        </button>

    val returnLink: NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))

    Helpers.bind("plsolveform", template,
      "alphabettext" -> alphabetText,
      "languagedescription" -> language,
      "constraintdescription" -> constraint,
      "pumpingstringfield" -> pumpingStField,
      "generatesplitsbutton" -> generateSplitsButton,
      "returnlink" -> returnLink)

  }

  override def onDelete(generalProblem: Problem): Unit = {

  }
}