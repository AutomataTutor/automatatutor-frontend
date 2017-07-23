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

class WordsInGrammarProblem extends LongKeyedMapper[WordsInGrammarProblem] with IdPK with SpecificProblem[WordsInGrammarProblem] {
	def getSingleton = WordsInGrammarProblem

	object problemId extends MappedLongForeignKey(this, Problem)
	object grammar extends MappedText(this)
	object inNeeded extends MappedInt(this)
	object outNeeded extends MappedInt(this)
	
	def getGrammar = this.grammar.is
	def setGrammar(g : String) = this.grammar(g)
	def getInNeeded = this.inNeeded.is
	def setInNeeded(i : Int) = this.inNeeded(i)
	def getOutNeeded = this.outNeeded.is
	def setOutNeeded(i : Int) = this.outNeeded(i)

	override def copy(): WordsInGrammarProblem = {
	  val retVal = new WordsInGrammarProblem
	  retVal.problemId(this.problemId.get)
	  retVal.grammar(this.grammar.get)
	  retVal.inNeeded(this.inNeeded.get)
	  retVal.outNeeded(this.outNeeded.get)
	  return retVal
	}
	
	override def setGeneralProblem(newProblem: Problem) = this.problemId(newProblem)
	
}

object WordsInGrammarProblem extends WordsInGrammarProblem with LongKeyedMetaMapper[WordsInGrammarProblem] {
	def findByGeneralProblem(generalProblem : Problem) : WordsInGrammarProblem =
	  find(By(WordsInGrammarProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a WordsInGrammarProblem")

	def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    this.bulkDelete_!!(By(WordsInGrammarProblem.problemId, generalProblem))
}