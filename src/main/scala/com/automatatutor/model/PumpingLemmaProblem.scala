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

class PumpingLemmaProblem extends LongKeyedMapper[PumpingLemmaProblem] with IdPK {
	def getSingleton = PumpingLemmaProblem

	object problemId extends MappedLongForeignKey(this, Problem)
	object language extends MappedText(this)
	object constraint extends MappedText(this)	
	object alphabet extends MappedText(this)
	object pumpingString extends MappedText(this)
	
	def getAlphabet : Seq[String] = this.alphabet.is.split(" ")
}

object PumpingLemmaProblem extends PumpingLemmaProblem with LongKeyedMetaMapper[PumpingLemmaProblem] {
	def findByGeneralProblem(generalProblem : Problem) : PumpingLemmaProblem =
	  find(By(PumpingLemmaProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a PumpingLemmaProblem")
}