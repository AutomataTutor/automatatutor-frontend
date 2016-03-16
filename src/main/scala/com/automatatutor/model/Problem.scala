package com.automatatutor.model

import com.automatatutor.snippet.DFAConstructionSnippet
import com.automatatutor.snippet.NFAProblemSnippet
import com.automatatutor.snippet.NFAToDFAProblemSnippet
import com.automatatutor.snippet.ProblemSnippet
import com.automatatutor.snippet.RegExConstructionSnippet
import com.automatatutor.snippet.PumpingLemmaProblemSnippet
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedLong
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedText
import net.liftweb.mapper.MappedLongForeignKey
import bootstrap.liftweb.StartupHook

class ProblemType extends LongKeyedMapper[ProblemType] with IdPK {
	def getSingleton = ProblemType
	
	val DFAConstructionTypeName = "English to DFA"
	val NFAConstructionTypeName = "English to NFA"
	val NFAToDFATypeName = "NFA to DFA"
	val EnglishToRegExTypeName = "English to Regular Expression"
	val PLTypeName = "Pumping Lemma Proof"

    val knownProblemTypes : Map[String, ProblemSnippet] = Map(
        DFAConstructionTypeName -> DFAConstructionSnippet,
        NFAConstructionTypeName -> NFAProblemSnippet,
        NFAToDFATypeName -> NFAToDFAProblemSnippet,
        EnglishToRegExTypeName -> RegExConstructionSnippet,
        PLTypeName -> PumpingLemmaProblemSnippet)
	
	object problemTypeName extends MappedString(this, 200)
  
	def getProblemSnippet() : ProblemSnippet = knownProblemTypes(this.problemTypeName.is)
	
	def getSpecificProblem(generalProblem: Problem): SpecificProblem[_] = this.problemTypeName.get match {
	  case DFAConstructionTypeName => DFAConstructionProblem.findByGeneralProblem(generalProblem)
	  case NFAConstructionTypeName => NFAConstructionProblem.findByGeneralProblem(generalProblem)
	  case NFAToDFATypeName => NFAToDFAProblem.findByGeneralProblem(generalProblem)
	  case EnglishToRegExTypeName => RegExConstructionProblem.findByGeneralProblem(generalProblem)
	  case PLTypeName => PumpingLemmaProblem.findByGeneralProblem(generalProblem)
	}
}

object ProblemType extends ProblemType with LongKeyedMetaMapper[ProblemType] with StartupHook {

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
	
	def shareWithUserByEmail(email: String) : Boolean = {
	  val otherUser = User.findByEmail(email) match {
	    case Full(user) => user
	    case _ => return false
	  }
	  
	  val copiedGeneralProblem = new Problem
	  copiedGeneralProblem.problemType(this.problemType.get)
	  copiedGeneralProblem.makePrivate
	  copiedGeneralProblem.createdBy(otherUser)
	  copiedGeneralProblem.shortDescription(this.shortDescription.get)
	  copiedGeneralProblem.longDescription(this.longDescription.get)
	  copiedGeneralProblem.save()
	  
	  val copiedSpecificProblem: SpecificProblem[_] = this.problemType.obj.openOrThrowException("Every problem must have an associated type").getSpecificProblem(this).copy()
	  copiedSpecificProblem.setGeneralProblem(copiedGeneralProblem)
	  copiedSpecificProblem.save()
	  
	  return true
	}
}

object Problem extends Problem with LongKeyedMetaMapper[Problem] {
  def findAllByCreator(creator : User) : List[Problem] = findAll(By(Problem.createdBy, creator))
  def findPublicProblems : List[Problem] = findAll(By(Problem.visibility, 0))
}

abstract trait SpecificProblem[T] {
  /// Does not save the modified problem. Caller has to do that manually by calling save()
  def setGeneralProblem(newProblem: Problem)
  def save(): Boolean
  def copy(): SpecificProblem[T]
}