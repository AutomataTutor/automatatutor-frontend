package com.automatatutor.snippet

import scala.xml.NodeSeq
import scala.xml.Text

import com.automatatutor.lib.TableHelper
import com.automatatutor.model.PosedProblem
import com.automatatutor.model.ProblemSet

import net.liftweb.http.SHtml

class Practicesets {
  
  def renderindex ( ignored : NodeSeq ) : NodeSeq = {
    val practiceSets = ProblemSet.getPracticeSets
    val practiceSetsNodeSeq = {
        val practiceSetsTable = TableHelper.renderTableWithHeader(practiceSets,
            ("Name", (problemSet : ProblemSet) => Text(problemSet.getName)),
            ("Problems", (problemSet : ProblemSet) => Text(problemSet.getPosedProblems.size.toString)))
            

        val problemSetsHeaderRow = (practiceSetsTable \ "tr").head
        val problemSetsDataRows : NodeSeq = (practiceSetsTable \ "tr").tail
        val problemSetsWithRows = practiceSets.zip(problemSetsDataRows)
        val problemSetsWithProblemsRows = problemSetsWithRows.flatMap(tuple => {
        	val practiceSet = tuple._1
        	val practiceSetRow = tuple._2
        	val problemsTable : NodeSeq = renderSolvePracticeSetNoHeader(practiceSet)
        	practiceSetRow ++ <tr> <td colspan="4"> { problemsTable } </td> </tr>
        })

        <table> { problemSetsHeaderRow } { problemSetsWithProblemsRows } </table>
    }
    
    return practiceSetsNodeSeq
  }

 private def renderSolvePracticeSetNoHeader( practiceSet : ProblemSet ) : NodeSeq = {
    val posedProblems = practiceSet.getPosedProblems
    
    return TableHelper.renderTable(posedProblems, 
        (posedProblem : PosedProblem) => Text(posedProblem.getProblem.shortDescription.is),
        (posedProblem : PosedProblem) => Text(posedProblem.getProblem.getTypeName),
        (posedProblem : PosedProblem) => SHtml.link("/practicesets/solve", () => { PosedProblemReqVar(posedProblem) }, Text("solve")))
  }  

}