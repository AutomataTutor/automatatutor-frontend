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

class NFAConstructionProblemCategory extends LongKeyedMapper[NFAConstructionProblemCategory] with IdPK {
	val knownConstructionTypes = List("Starts with", "Ends with", "Find substring", "Counting", "Counting Modulo", "Other")
	def getSingleton = NFAConstructionProblemCategory

	object categoryName extends MappedString(this, 40)
}

object NFAConstructionProblemCategory extends NFAConstructionProblemCategory with LongKeyedMetaMapper[NFAConstructionProblemCategory] {
  def onStartup = {
    knownConstructionTypes.map(assertExists(_))
  }
  
  def assertExists(typeName : String) : Unit = if (!exists(typeName)) { NFAConstructionProblemCategory.create.categoryName(typeName).save }
  def exists (typeName : String) : Boolean = !findAll(By(NFAConstructionProblemCategory.categoryName, typeName)).isEmpty
}

class NFAConstructionProblem extends LongKeyedMapper[NFAConstructionProblem] with IdPK {
	def getSingleton = NFAConstructionProblem

	object problemId extends MappedLongForeignKey(this, Problem)
	object automaton extends MappedText(this)
	object category extends MappedLongForeignKey(this, NFAConstructionProblemCategory)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	// Since we have ε in the alphabet, we have to remove it before handing out the alphabet
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)
	
	def getEpsilon : Boolean = 
		(getXmlDescription \ "epsilon").isEmpty || 
		(getXmlDescription \ "epsilon").map(_.text).head.replaceAll(" ", "").toBoolean
}

object NFAConstructionProblem extends NFAConstructionProblem with LongKeyedMetaMapper[NFAConstructionProblem] {
	def findByGeneralProblem(generalProblem : Problem) : NFAConstructionProblem =
	  find(By(NFAConstructionProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a NFAConstructionProblem")
}