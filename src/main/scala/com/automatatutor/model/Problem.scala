package com.automatatutor.model

import com.automatatutor.snippet.DFAConstructionSnippet
import com.automatatutor.snippet.NFAProblemSnippet
import com.automatatutor.snippet.NFAToDFAProblemSnippet
import com.automatatutor.snippet.ProblemSnippet
import com.automatatutor.snippet.RegExConstructionSnippet
import com.automatatutor.snippet.WordsInGrammarSnippet
import com.automatatutor.snippet.DescriptionToGrammarSnippet
import com.automatatutor.snippet.CYKProblemSnippet
import com.automatatutor.snippet.GrammarToCNFSnippet
import com.automatatutor.snippet.MinimizationSnippet
import com.automatatutor.snippet.ProductConstructionSnippet
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
import com.automatatutor.lib.Config
import com.automatatutor.BuchiGameSolving

class ProblemType extends LongKeyedMapper[ProblemType] with IdPK {
	def getSingleton = ProblemType
	
	val DFAConstructionTypeName = "English to DFA"
	val NFAConstructionTypeName = "English to NFA"
	val NFAToDFATypeName = "NFA to DFA"
	val EnglishToRegExTypeName = "English to Regular Expression"
	val PLTypeName = "Pumping Lemma Proof"
	val BuchiSolvingTypeName = "Buchi Game Solving"
	val WordsInGrammarTypeName = "Words in Grammar"
	val DescriptionToGrammarTypeName = "English to Grammar"
	val GrammarToCNFTypeName = "Grammar to CNF"
	val CYKTypeName = "CYK Algorithm"
	val ProductConstructionTypeName = "Product Construction"
	val MinimizationTypeName = "Minimization"

    val knownProblemTypes : Map[String, ProblemSnippet] = Map(
        DFAConstructionTypeName -> DFAConstructionSnippet,
        NFAConstructionTypeName -> NFAProblemSnippet,
        NFAToDFATypeName -> NFAToDFAProblemSnippet,
        EnglishToRegExTypeName -> RegExConstructionSnippet,
        WordsInGrammarTypeName -> WordsInGrammarSnippet,
        DescriptionToGrammarTypeName -> DescriptionToGrammarSnippet,
        GrammarToCNFTypeName -> GrammarToCNFSnippet,
        CYKTypeName -> CYKProblemSnippet,
			  ProductConstructionTypeName -> ProductConstructionSnippet,
			MinimizationTypeName -> MinimizationSnippet
        ) ++
        (if(Config.buchiGameSolving.enabled.get) { Map(BuchiSolvingTypeName -> BuchiGameSolving.SnippetAdapter) } else { Map[String, ProblemSnippet]() })
	
	protected object problemTypeName extends MappedString(this, 200)
	
	def getProblemTypeName = this.problemTypeName.is
	def setProblemTypeName(problemTypeName : String) = this.problemTypeName(problemTypeName)
  
	def getProblemSnippet() : ProblemSnippet = knownProblemTypes(this.problemTypeName.is)
	
	def getSpecificProblem(generalProblem: Problem): SpecificProblem[_] = this.problemTypeName.get match {
	  case DFAConstructionTypeName => DFAConstructionProblem.findByGeneralProblem(generalProblem)
		case ProductConstructionTypeName => ProductConstructionProblem.findByGeneralProblem(generalProblem)
		case MinimizationTypeName => MinimizationProblem.findByGeneralProblem(generalProblem)
	  case NFAConstructionTypeName => NFAConstructionProblem.findByGeneralProblem(generalProblem)
	  case NFAToDFATypeName => NFAToDFAProblem.findByGeneralProblem(generalProblem)
	  case EnglishToRegExTypeName => RegExConstructionProblem.findByGeneralProblem(generalProblem)
	  case WordsInGrammarTypeName => WordsInGrammarProblem.findByGeneralProblem(generalProblem)
	  case DescriptionToGrammarTypeName => DescriptionToGrammarProblem.findByGeneralProblem(generalProblem)
	  case GrammarToCNFTypeName => GrammarToCNFProblem.findByGeneralProblem(generalProblem)
	  case CYKTypeName => CYKProblem.findByGeneralProblem(generalProblem)
	}
}

object ProblemType extends ProblemType with LongKeyedMetaMapper[ProblemType] with StartupHook {

  def onStartup = knownProblemTypes.map(entry => assertExists(entry._1))
  
  def assertExists(typeName : String) : Unit = if (!exists(typeName)) { ProblemType.create.problemTypeName(typeName).save }
  def exists (typeName : String) : Boolean = !findAll(By(ProblemType.problemTypeName, typeName)).isEmpty
}

class Problem extends LongKeyedMapper[Problem] with IdPK {
	def getSingleton = Problem

	protected object problemType extends MappedLongForeignKey(this, ProblemType)
	protected object visibility extends MappedLong(this)
	protected object createdBy extends MappedLongForeignKey(this, User)
	protected object shortDescription extends MappedText(this)
	protected object longDescription extends MappedText(this)
	
	def getProblemType = this.problemType.obj openOrThrowException "Every Problem must have a ProblemType"
	def setProblemType(problemType : ProblemType) = this.problemType(problemType)
	
	def getTypeName() : String = (problemType.obj.map(_.getProblemTypeName)) openOr ""
	
	def isPublic = this.visibility.is == 0
	def isPrivate = this.visibility.is == 1
	
	def makePublic = this.visibility(0)
	def makePrivate = this.visibility(1)
	
	def toggleVisibility = if(this.isPublic) { makePrivate } else { makePublic }
	
	def getCreator : User = this.createdBy.obj openOrThrowException "Every Problem must have a CreatedBy"
	def setCreator( creator : User ) = this.createdBy(creator)
	
	def getShortDescription = this.shortDescription.is
	def setShortDescription( description : String ) = this.shortDescription(description)
	
	def getLongDescription = this.longDescription.is
	def setLongDescription( description : String ) = this.longDescription(description)
	
	def isPosed : Boolean = PosedProblem.existsForProblem(this)
	
	def canBeDeleted : Boolean = return !(this.isPosed || this.isPublic)
	def getDeletePreventers : Seq[String] = {
	  return (if(this.isPosed) { List("Problem is posed in some problem set")} else { List() }) ++ (if(this.isPublic) { List("Problem is public") } else { List() })
	}
	override def delete_! : Boolean = if(!this.canBeDeleted) { return false } else { 	  
	  return super.delete_! 	  
	}
	
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
  def setGeneralProblem(newProblem: Problem) : T
  def save(): Boolean
  def copy(): SpecificProblem[T]
}