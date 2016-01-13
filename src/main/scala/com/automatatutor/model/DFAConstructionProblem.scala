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

class DFAConstructionProblemCategory extends LongKeyedMapper[DFAConstructionProblemCategory] with IdPK {
	val knownConstructionTypes = List("Starts with", "Ends with", "Find substring", "Counting", "Counting Modulo", "Other")
	def getSingleton = DFAConstructionProblemCategory

	object categoryName extends MappedString(this, 40)
}

object DFAConstructionProblemCategory extends DFAConstructionProblemCategory with LongKeyedMetaMapper[DFAConstructionProblemCategory] {
  def onStartup = {
    knownConstructionTypes.map(assertExists(_))
  }
  
  def assertExists(typeName : String) : Unit = if (!exists(typeName)) { DFAConstructionProblemCategory.create.categoryName(typeName).save }
  def exists (typeName : String) : Boolean = !findAll(By(DFAConstructionProblemCategory.categoryName, typeName)).isEmpty
}

class DFAConstructionProblem extends LongKeyedMapper[DFAConstructionProblem] with IdPK with SpecificProblem[DFAConstructionProblem] {
	def getSingleton = DFAConstructionProblem

	object problemId extends MappedLongForeignKey(this, Problem)
	object automaton extends MappedText(this)
	object category extends MappedLongForeignKey(this, DFAConstructionProblemCategory)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)
	
	override def copy(): DFAConstructionProblem = {
	  val retVal = new DFAConstructionProblem
	  retVal.problemId(this.problemId.get)
	  retVal.automaton(this.automaton.get)
	  retVal.category(this.category.get)
	  return retVal
	}
	
	override def setGeneralProblem(newProblem: Problem) = this.problemId(newProblem)
}

object DFAConstructionProblem extends DFAConstructionProblem with LongKeyedMetaMapper[DFAConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : DFAConstructionProblem =
	  find(By(DFAConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a DFAConstructionProblem")
}