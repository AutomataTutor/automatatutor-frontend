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

class NFAToDFAProblem extends LongKeyedMapper[NFAToDFAProblem] with IdPK {
	def getSingleton = NFAToDFAProblem

	object problemId extends MappedLongForeignKey(this, Problem)
	object automaton extends MappedText(this)
	
	def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)
	
	def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)
}

object NFAToDFAProblem extends NFAToDFAProblem with LongKeyedMetaMapper[NFAToDFAProblem] {
	def findByGeneralProblem(generalProblem : Problem) : NFAToDFAProblem =
	  find(By(NFAToDFAProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a DFAConstructionProblem")
}