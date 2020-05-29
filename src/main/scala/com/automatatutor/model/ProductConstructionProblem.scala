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

class ProductConstructionProblem extends LongKeyedMapper[ProductConstructionProblem] with IdPK with SpecificProblem[ProductConstructionProblem] {
	def getSingleton = ProductConstructionProblem

	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected class automatonClass extends MappedText(this) {

	}
	protected class booleanOperationClass extends MappedText(this) {

	}
	protected val automaton1 : automatonClass = new automatonClass
	protected val automaton2 : automatonClass = new automatonClass
	protected val automataList : List[automatonClass] = List(automaton1, automaton2)
	protected val booleanOperation : booleanOperationClass = new booleanOperationClass

	def getGeneralProblem = this.problemId.obj openOrThrowException "Every ProductConstructionProblem must have a ProblemId"
	override def setGeneralProblem(problem : Problem) : ProductConstructionProblem = this.problemId(problem)

	def getAutomataList = this.automataList
	def getBooleanOperation = this.booleanOperation
	def setAutomaton1(automaton : String) = this.automataList(0)(automaton)
	def setAutomaton2(automaton : String) = this.automataList(1)(automaton)
	def setBooleanOperation(boolOp : String) = this.booleanOperation(boolOp)
	def setAutomaton1(automaton : NodeSeq) = this.automataList(0)(automaton.mkString)
	def setAutomaton2(automaton : NodeSeq) = this.automataList(1)(automaton.mkString)
	
	def getXmlDescription1 : NodeSeq = XML.loadString(this.automataList(0).is)
	def getXmlDescription2 : NodeSeq = XML.loadString(this.automataList(1).is)
	
	def getAlphabet : Seq[String] = (getXmlDescription1 \ "alphabet" \ "symbol").map(_.text)
	
	override def copy(): ProductConstructionProblem = {
	  val retVal = new ProductConstructionProblem
	  retVal.problemId(this.problemId.get)
		retVal.automataList(0)(this.automataList(0).get)
	  return retVal
	}
}

object ProductConstructionProblem extends ProductConstructionProblem with LongKeyedMetaMapper[ProductConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : ProductConstructionProblem =
	  find(By(ProductConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a ProductConstructionProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    bulkDelete_!!(By(ProductConstructionProblem.problemId, generalProblem))
}