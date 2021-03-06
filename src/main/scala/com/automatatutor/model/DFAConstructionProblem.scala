package com.automatatutor.model

import net.liftweb.mapper.MappedString
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedLongForeignKey
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.By
import net.liftweb.mapper.MappedText
import scala.xml.XML
import scala.xml.NodeSeq
import bootstrap.liftweb.StartupHook

class DFAConstructionProblem extends LongKeyedMapper[DFAConstructionProblem] with IdPK with SpecificProblem[DFAConstructionProblem] {
	def getSingleton = DFAConstructionProblem

	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected object automaton extends MappedText(this)
	
	def getGeneralProblem = this.problemId.obj openOrThrowException "Every DFAConstructionProblem must have a ProblemId"
	override def setGeneralProblem(problem : Problem) : DFAConstructionProblem = this.problemId(problem)
	
	def getAutomaton = this.automaton.is
	def setAutomaton(automaton : String) = this.automaton(automaton)
	def setAutomaton(automaton : NodeSeq) = this.automaton(automaton.mkString)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)
	
	override def copy(): DFAConstructionProblem = {
	  val retVal = new DFAConstructionProblem
	  retVal.problemId(this.problemId.get)
	  retVal.automaton(this.automaton.get)
	  return retVal
	}
}

object DFAConstructionProblem extends DFAConstructionProblem with LongKeyedMetaMapper[DFAConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : DFAConstructionProblem =
	  find(By(DFAConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a DFAConstructionProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    bulkDelete_!!(By(DFAConstructionProblem.problemId, generalProblem))
}