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

class NFAToDFAProblem extends LongKeyedMapper[NFAToDFAProblem] with IdPK with SpecificProblem[NFAToDFAProblem] {
	def getSingleton = NFAToDFAProblem

	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected object automaton extends MappedText(this)
	
	def getGeneralProblem = this.problemId.obj openOrThrowException "Every NFAToDFAProblem must have a ProblemId"
	override def setGeneralProblem(problem : Problem) = this.problemId(problem)
	
	def getAutomaton = this.automaton.get
	def setAutomaton( automaton : String ) = this.automaton(automaton)
	def setAutomaton( automaton : NodeSeq ) = this.automaton(automaton.mkString)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)

	override def copy(): NFAToDFAProblem = {
	  val retVal = new NFAToDFAProblem
	  retVal.problemId(this.problemId.get)
	  retVal.automaton(this.automaton.get)
	  return retVal
	}
}

object NFAToDFAProblem extends NFAToDFAProblem with LongKeyedMetaMapper[NFAToDFAProblem] {
	def findByGeneralProblem(generalProblem : Problem) : NFAToDFAProblem =
	  find(By(NFAToDFAProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a DFAConstructionProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    NFAToDFAProblem.bulkDelete_!!(By(NFAToDFAProblem.problemId, generalProblem))
}