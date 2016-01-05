package com.automatatutor.model

import net.liftweb.mapper._
import net.liftweb.common.Full
import net.liftweb.common.Empty

class PosedProblem extends LongKeyedMapper[PosedProblem] with IdPK {
	def getSingleton = PosedProblem

	object allowedAttempts extends MappedLong(this)
	object maxGrade extends MappedLong(this)
	object problemId extends MappedLongForeignKey(this, Problem)
	object nextPosedProblemId extends MappedLongForeignKey(this, PosedProblem)
	
	def deleteRecursively : Unit = {
	  this.nextPosedProblemId.obj match {
	    case Full(nextPosedProblem) => nextPosedProblem.deleteRecursively
	    case _ => {} // We've reached the end of recursion
	  }
	  this.delete_! // Delete self
	  return ()
	}
	
	def getListRecursively : List[PosedProblem] = {
	  return this :: ( this.nextPosedProblemId.obj match {
	    case Full(nextProblem) => nextProblem.getListRecursively
	    case _ => Nil
	  } )
	}
	
	def removeProblemRecursively ( toRemove : PosedProblem ) : Unit = {
	  this.nextPosedProblemId.obj match {
	    case Full(nextProblem) => if (nextProblem.equals(toRemove)) { 
	    	this.nextPosedProblemId(nextProblem.nextPosedProblemId.obj).save
	    	nextProblem.delete_!
	      } else { 
	        nextProblem.removeProblemRecursively(toRemove)
	      }
	    case _ => {} // We cannot remove the problem if it is not in the list, thus do nothing
	  }
	}
	
	def appendProblemRecursively ( toAppend : PosedProblem )  : Unit = {
	  this.nextPosedProblemId.obj match {
	    case Full(nextPosedProblem) => nextPosedProblem.appendProblemRecursively(toAppend)
	    case _ => this.nextPosedProblemId(toAppend).save
	  }
	}
	
	def getAttempts ( user : User, problemSet : PosedProblemSet ) : Seq[SolutionAttempt] = {
	  return SolutionAttempt.findAll(
	      By(SolutionAttempt.userId, user), 
	      By(SolutionAttempt.posedProblemSetId, problemSet),
	      By(SolutionAttempt.posedProblemId, this))
	}
	
	def getNumberAttempts( user : User, problemSet : PosedProblemSet ) : Int = {
	  this.getAttempts(user, problemSet).size
	}
	
	def getNumberAttemptsRemaining(user : User, problemSet : PosedProblemSet ) : Long = {

	  if(this.isPracticeProblem) { 
	    return 1 
	  } else {
		return this.allowedAttempts.is - this.getNumberAttempts(user, problemSet)
	  }
	}
	
	def getGrade ( user : User, problemSet : PosedProblemSet ) : Int = {
	  val grades = this.getAttempts(user, problemSet).map(_.grade.is)
	  if(grades.isEmpty) {
	    return 0
	  } else {
	    return grades.max
	  }
	}
	
	def getMaxGrade : Long = this.maxGrade.is
	
	/**
	 * A posed problem is defined as open if either the user has used all attempts
	 * or if they have reached the maximal possible grade in one attempt
	 */
	def isOpen ( user : User, problemSet : PosedProblemSet ) : Boolean = {
	  val allowedAttempts = this.allowedAttempts.is
	  val takenAttempts = this.getNumberAttempts(user, problemSet)
	  val maxGrade = this.maxGrade.is
	  val userGrade = this.getGrade(user, problemSet)
	  
	  return isPracticeProblem || (takenAttempts < allowedAttempts && userGrade < maxGrade)
	}
	
	def isPracticeProblem = allowedAttempts == 0
	
	def getProblem : Problem = this.problemId.obj openOrThrowException "Every Posed Problem must be associated with a Problem"
}

object PosedProblem extends PosedProblem with LongKeyedMetaMapper[PosedProblem] {
	def existsForProblem(problem : Problem) : Boolean = this.find(By(PosedProblem.problemId, problem)) match { 
	case Empty => false case Full(_) => true case _ => false}
	def findByProblem(problem : Problem) : Seq[PosedProblem] = this.findAll(By(PosedProblem.problemId, problem))
}
