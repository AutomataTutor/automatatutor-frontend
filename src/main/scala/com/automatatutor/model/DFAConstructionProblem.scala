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

class DFAConstructionProblemCategory extends LongKeyedMapper[DFAConstructionProblemCategory] with IdPK {
	val knownConstructionTypes = List("Starts with", "Ends with", "Find substring", "Counting", "Counting Modulo", "Other")
	def getSingleton = DFAConstructionProblemCategory

	protected object categoryName extends MappedString(this, 40)
	
	def getCategoryName = this.categoryName.is
	def setCategoryName(categoryName : String) = this.categoryName(categoryName)
}

object DFAConstructionProblemCategory extends DFAConstructionProblemCategory with LongKeyedMetaMapper[DFAConstructionProblemCategory] with StartupHook {
  def onStartup = {
    knownConstructionTypes.map(assertExists(_))
  }
  
  def assertExists(typeName : String) : Unit = if (!exists(typeName)) { DFAConstructionProblemCategory.create.categoryName(typeName).save }
  def exists (typeName : String) : Boolean = !findAll(By(DFAConstructionProblemCategory.categoryName, typeName)).isEmpty
}

class DFAConstructionProblem extends LongKeyedMapper[DFAConstructionProblem] with IdPK with SpecificProblem[DFAConstructionProblem] {
	def getSingleton = DFAConstructionProblem

	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected object automaton extends MappedText(this)
	protected object category extends MappedLongForeignKey(this, DFAConstructionProblemCategory)
	
	def getGeneralProblem = this.problemId.obj openOrThrowException "Every DFAConstructionProblem must have a ProblemId"
	override def setGeneralProblem(problem : Problem) : DFAConstructionProblem = this.problemId(problem)
	
	def getAutomaton = this.automaton.is
	def setAutomaton(automaton : String) = this.automaton(automaton)
	def setAutomaton(automaton : NodeSeq) = this.automaton(automaton.mkString)
	
	def getCategory = this.category.obj openOrThrowException "Every DFAConstructionProblem must have a Category"
	def setCategory(category : DFAConstructionProblemCategory) = this.category(category)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)
	
	override def copy(): DFAConstructionProblem = {
	  val retVal = new DFAConstructionProblem
	  retVal.problemId(this.problemId.get)
	  retVal.automaton(this.automaton.get)
	  retVal.category(this.category.get)
	  return retVal
	}
}

object DFAConstructionProblem extends DFAConstructionProblem with LongKeyedMetaMapper[DFAConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : DFAConstructionProblem =
	  find(By(DFAConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a DFAConstructionProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    bulkDelete_!!(By(DFAConstructionProblem.problemId, generalProblem))
}