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

class NFAConstructionProblemCategory extends LongKeyedMapper[NFAConstructionProblemCategory] with IdPK {
	val knownConstructionTypes = List("Starts with", "Ends with", "Find substring", "Counting", "Counting Modulo", "Other")
	def getSingleton = NFAConstructionProblemCategory

	protected object categoryName extends MappedString(this, 40)
	
	def getCategoryName = this.categoryName.is
	def setCategoryName(categoryName : String) = this.categoryName(categoryName)
}

object NFAConstructionProblemCategory extends NFAConstructionProblemCategory with LongKeyedMetaMapper[NFAConstructionProblemCategory] with StartupHook {
  def onStartup = {
    knownConstructionTypes.map(assertExists(_))
  }
  
  def assertExists(typeName : String) : Unit = if (!exists(typeName)) { NFAConstructionProblemCategory.create.categoryName(typeName).save }
  def exists (typeName : String) : Boolean = !findAll(By(NFAConstructionProblemCategory.categoryName, typeName)).isEmpty
}

class NFAConstructionProblem extends LongKeyedMapper[NFAConstructionProblem] with IdPK with SpecificProblem[NFAConstructionProblem] {
	def getSingleton = NFAConstructionProblem

	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected object automaton extends MappedText(this)
	protected object category extends MappedLongForeignKey(this, NFAConstructionProblemCategory)
	
	def getGeneralProblem = this.problemId.obj openOrThrowException "Every NFAConstructionProblem must have a ProblemId"
	override def setGeneralProblem(newProblem: Problem) = this.problemId(newProblem)
	
	def getAutomaton = this.automaton.get
	def setAutomaton(automaton : String) = this.automaton(automaton)
	def setAutomaton(automaton : NodeSeq) = this.automaton(automaton.mkString)
	
	def getCategory = this.category.obj openOrThrowException "Every NFAConstructionProblem must have a Category"
	def setCategory(category : NFAConstructionProblemCategory) = this.category(category)
	
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
	  retVal.category(this.category.get)
	  return retVal
	}
}

object NFAConstructionProblem extends NFAConstructionProblem with LongKeyedMetaMapper[NFAConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : NFAConstructionProblem =
	  find(By(NFAConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a NFAConstructionProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    this.bulkDelete_!!(By(NFAConstructionProblem.problemId, generalProblem))
}