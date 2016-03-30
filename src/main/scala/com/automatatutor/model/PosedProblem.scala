package com.automatatutor.model

import net.liftweb.mapper._
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.common.Box

class PosedProblem extends LongKeyedMapper[PosedProblem] with IdPK {
	def getSingleton = PosedProblem

	protected object allowedAttempts extends MappedLong(this)
	protected object maxGrade extends MappedLong(this)
	protected object problemId extends MappedLongForeignKey(this, Problem)
	protected object nextPosedProblemId extends MappedLongForeignKey(this, PosedProblem)
	
	def getAllowedAttempts : Long = this.allowedAttempts.is
	def setAllowedAttempts ( attempts : Long ) : PosedProblem = this.allowedAttempts(attempts)
	
	def getMaxGrade : Long = this.maxGrade.is
	def setMaxGrade ( maxGrade : Long ) = this.maxGrade(maxGrade)
	
	def getProblem : Problem = this.problemId.obj openOrThrowException "Every PosedProblem must have a Problem"
	def setProblem ( problem : Problem ) = this.problemId(problem)
	
	def getNextPosedProblem : Box[PosedProblem] = this.nextPosedProblemId.obj
	def setNextPosedProblem ( posedProblem : PosedProblem ) : PosedProblem = this.nextPosedProblemId(posedProblem)
	def setNextPosedProblem ( posedProblem : Box[PosedProblem] ) : PosedProblem = this.nextPosedProblemId(posedProblem)
	
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
}

object PosedProblem extends PosedProblem with LongKeyedMetaMapper[PosedProblem] {
	def existsForProblem(problem : Problem) : Boolean = this.find(By(PosedProblem.problemId, problem)) match { 
	case Empty => false case Full(_) => true case _ => false}
	def findByProblem(problem : Problem) : Seq[PosedProblem] = this.findAll(By(PosedProblem.problemId, problem))
}
