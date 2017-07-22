package com.automatatutor.model

import scala.xml.NodeSeq
import scala.xml.XML
import net.liftweb.mapper.By
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedText
import net.liftweb.mapper.MappedInt
import net.liftweb.mapper.MappedLongForeignKey
import bootstrap.liftweb.StartupHook

class CYKProblem extends LongKeyedMapper[CYKProblem] with IdPK with SpecificProblem[CYKProblem] {
	def getSingleton = CYKProblem

	object problemId extends MappedLongForeignKey(this, Problem)
	object grammar extends MappedText(this)
	object word extends MappedText(this)
	
	def getGrammar = this.grammar.is
	def setGrammar(g : String) = this.grammar(g)
	def getWord = this.word.is
	def setWord(w : String) = this.word(w)

	override def copy(): CYKProblem = {
	  val retVal = new CYKProblem
	  retVal.problemId(this.problemId.get)
	  retVal.grammar(this.grammar.get)
	  retVal.word(this.word.get)
	  return retVal
	}
	
	override def setGeneralProblem(newProblem: Problem) = this.problemId(newProblem)
	
}

object CYKProblem extends CYKProblem with LongKeyedMetaMapper[CYKProblem] {
	def findByGeneralProblem(generalProblem : Problem) : CYKProblem =
	  find(By(CYKProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a CYKProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    this.bulkDelete_!!(By(CYKProblem.problemId, generalProblem))
}