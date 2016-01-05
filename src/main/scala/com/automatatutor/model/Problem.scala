package com.automatatutor.model

import com.automatatutor.snippet.DFAConstructionSnippet
import com.automatatutor.snippet.NFAProblemSnippet
import com.automatatutor.snippet.NFAToDFAProblemSnippet
import com.automatatutor.snippet.ProblemSnippet
import com.automatatutor.snippet.RegExConstructionSnippet
import com.automatatutor.snippet.PumpingLemmaProblemSnippet
import net.liftweb.mapper.By
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedText
import net.liftweb.mapper.MappedLongForeignKey

class ProblemType extends LongKeyedMapper[ProblemType] with IdPK {
	def getSingleton = ProblemType

    val knownProblemTypes : Map[String, ProblemSnippet] = Map(
        "English to DFA" -> DFAConstructionSnippet,
        "English to NFA" -> NFAProblemSnippet,
        "NFA to DFA" -> NFAToDFAProblemSnippet,
        "English to Regular Expression" -> RegExConstructionSnippet,
        "Pumping Lemma Proof" -> PumpingLemmaProblemSnippet)
	
	object problemTypeName extends MappedString(this, 200)
  
	def getProblemSnippet() : ProblemSnippet = knownProblemTypes(this.problemTypeName.is)
}

object ProblemType extends ProblemType with LongKeyedMetaMapper[ProblemType] {

  def onStartup = knownProblemTypes.map(entry => assertExists(entry._1))
  
  def assertExists(typeName : String) : Unit = if (!exists(typeName)) { ProblemType.create.problemTypeName(typeName).save }
  def exists (typeName : String) : Boolean = !findAll(By(ProblemType.problemTypeName, typeName)).isEmpty
}

class Problem extends LongKeyedMapper[Problem] with IdPK {
	def getSingleton = Problem

	object problemType extends MappedLongForeignKey(this, ProblemType)
	object visibility extends MappedLong(this)
	object createdBy extends MappedLongForeignKey(this, User)
	object shortDescription extends MappedText(this)
	object longDescription extends MappedText(this)
	
	def getTypeName() : String = (problemType.obj.map(_.problemTypeName.is)) openOr ""
	def getType : ProblemType = this.problemType.obj openOrThrowException "Every problem must have an associated type"
	
	def isPublic = this.visibility.is == 0
	def isPrivate = this.visibility.is == 1
	
	def makePublic = this.visibility(0).save
	def makePrivate = this.visibility(1).save
	
	def toggleVisibility = if(this.isPublic) { makePrivate } else { makePublic }
	
	def isPosed : Boolean = PosedProblem.existsForProblem(this)
	
	def canBeDeleted : Boolean = return !(this.isPosed || this.isPublic)
	def getDeletePreventers : Seq[String] = {
	  return (if(this.isPosed) { List("Problem is posed in some problem set")} else { List() }) ++ (if(this.isPublic) { List("Problem is public") } else { List() })
	}
	override def delete_! : Boolean = if(!this.canBeDeleted) { return false } else { return super.delete_! }
}

object Problem extends Problem with LongKeyedMetaMapper[Problem] {
  def findAllByCreator(creator : User) : List[Problem] = findAll(By(Problem.createdBy, creator))
  def findPublicProblems : List[Problem] = findAll(By(Problem.visibility, 0))
}