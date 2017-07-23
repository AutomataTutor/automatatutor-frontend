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

class MinimizationProblem extends LongKeyedMapper[MinimizationProblem] with IdPK with SpecificProblem[MinimizationProblem] {
  def getSingleton = MinimizationProblem

  protected object problemId extends MappedLongForeignKey(this, Problem)
  protected class automatonClass extends MappedText(this) {

  }
  protected val automaton: automatonClass = new automatonClass

  def getGeneralProblem = this.problemId.obj openOrThrowException "Every MinimizationProblem must have a ProblemId"
  override def setGeneralProblem(problem : Problem) : MinimizationProblem = this.problemId(problem)

  def getAutomaton = this.automaton.is
  def setAutomaton(automaton : String) = this.automaton(automaton)
  def setAutomaton(automaton : NodeSeq) = this.automaton(automaton.mkString)

  def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)

  def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)

  override def copy(): MinimizationProblem = {
    val retVal = new MinimizationProblem
    retVal.problemId(this.problemId.get)
    retVal.automaton(this.automaton.get)
    return retVal
  }
}

object MinimizationProblem extends MinimizationProblem with LongKeyedMetaMapper[MinimizationProblem] {
  def findByGeneralProblem(generalProblem : Problem) : MinimizationProblem =
    find(By(MinimizationProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a MinimizationProblem")

  def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    bulkDelete_!!(By(MinimizationProblem.problemId, generalProblem))
}