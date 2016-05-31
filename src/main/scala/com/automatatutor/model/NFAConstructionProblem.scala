package com.automatatutor.model

import scala.xml.NodeSeq
import scala.xml.XML
import net.liftweb.mapper.By
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedText
import net.liftweb.mapper.MappedLongForeignKey
import bootstrap.liftweb.StartupHook

class NFAConstructionProblem extends LongKeyedMapper[NFAConstructionProblem] with IdPK with SpecificProblem[NFAConstructionProblem] {
	def getSingleton = NFAConstructionProblem

	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected object automaton extends MappedText(this)
	
	def getGeneralProblem = this.problemId.obj openOrThrowException "Every NFAConstructionProblem must have a ProblemId"
	override def setGeneralProblem(newProblem: Problem) = this.problemId(newProblem)
	
	def getAutomaton = this.automaton.get
	def setAutomaton(automaton : String) = this.automaton(automaton)
	def setAutomaton(automaton : NodeSeq) = this.automaton(automaton.mkString)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	// Since we have ε in the alphabet, we have to remove it before handing out the alphabet
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)
	
	def getEpsilon : Boolean = 
		(getXmlDescription \ "epsilon").isEmpty || 
		(getXmlDescription \ "epsilon").map(_.text).head.replaceAll(" ", "").toBoolean

	override def copy(): NFAConstructionProblem = {
	  val retVal = new NFAConstructionProblem
	  retVal.problemId(this.problemId.get)
	  retVal.automaton(this.automaton.get)
	  return retVal
	}
}

object NFAConstructionProblem extends NFAConstructionProblem with LongKeyedMetaMapper[NFAConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : NFAConstructionProblem =
	  find(By(NFAConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a NFAConstructionProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    this.bulkDelete_!!(By(NFAConstructionProblem.problemId, generalProblem))
}