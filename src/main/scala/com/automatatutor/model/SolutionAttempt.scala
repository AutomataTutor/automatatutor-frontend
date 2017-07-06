package com.automatatutor.model

import net.liftweb.mapper._
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full

class SolutionAttempt extends LongKeyedMapper[SolutionAttempt] with IdPK {
	def getSingleton = SolutionAttempt

	object dateTime extends MappedDateTime(this)
	object userId extends MappedLongForeignKey(this, User)
	object posedProblemSetId extends MappedLongForeignKey(this, PosedProblemSet)
	object posedProblemId extends MappedLongForeignKey(this, PosedProblem)
	object grade extends MappedInt(this)
}

object SolutionAttempt extends SolutionAttempt with LongKeyedMetaMapper[SolutionAttempt] {
	def getLatestAttempt(user : User, posedProblemSet : PosedProblemSet, posedProblem : PosedProblem) : Box[SolutionAttempt] = {
	  val allAttempts = this.findAll(By(SolutionAttempt.userId, user), By(SolutionAttempt.posedProblemSetId, posedProblemSet), By(SolutionAttempt.posedProblemId, posedProblem))
	  return if (allAttempts.isEmpty) { Empty } else { Full(allAttempts.maxBy(attempt => attempt.dateTime.is.getTime())) }
	}
}

class DFAConstructionSolutionAttempt extends LongKeyedMapper[DFAConstructionSolutionAttempt] with IdPK {
	def getSingleton = DFAConstructionSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptAutomaton extends MappedText(this)
}

object DFAConstructionSolutionAttempt extends DFAConstructionSolutionAttempt with LongKeyedMetaMapper[DFAConstructionSolutionAttempt] {
	def getByGeneralAttempt ( generalAttempt : SolutionAttempt ) : DFAConstructionSolutionAttempt = {
	  return this.find(By(DFAConstructionSolutionAttempt.solutionAttemptId, generalAttempt)) openOrThrowException "Must only be called if we are sure that the general attempt also has a DFA construction attempt"
	}
}

class NFAConstructionSolutionAttempt extends LongKeyedMapper[NFAConstructionSolutionAttempt] with IdPK {
	def getSingleton = NFAConstructionSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptAutomaton extends MappedText(this)
}

object NFAConstructionSolutionAttempt extends NFAConstructionSolutionAttempt with LongKeyedMetaMapper[NFAConstructionSolutionAttempt] {

}

class NFAToDFASolutionAttempt extends LongKeyedMapper[NFAToDFASolutionAttempt] with IdPK {
	def getSingleton = NFAToDFASolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptAutomaton extends MappedText(this)
}

object NFAToDFASolutionAttempt extends NFAToDFASolutionAttempt with LongKeyedMetaMapper[NFAToDFASolutionAttempt] {

}

class RegexConstructionSolutionAttempt extends LongKeyedMapper[RegexConstructionSolutionAttempt] with IdPK {
	def getSingleton = RegexConstructionSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptRegex extends MappedText(this)
}

object RegexConstructionSolutionAttempt extends RegexConstructionSolutionAttempt with LongKeyedMetaMapper[RegexConstructionSolutionAttempt] {

}

class WordsInGrammarSolutionAttempt extends LongKeyedMapper[WordsInGrammarSolutionAttempt] with IdPK {
	def getSingleton = WordsInGrammarSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptWordsIn extends MappedText(this)
	object attemptWordsOut extends MappedText(this)
}

object WordsInGrammarSolutionAttempt extends WordsInGrammarSolutionAttempt with LongKeyedMetaMapper[WordsInGrammarSolutionAttempt] {

}

class DescriptionToGrammarSolutionAttempt extends LongKeyedMapper[DescriptionToGrammarSolutionAttempt] with IdPK {
	def getSingleton = DescriptionToGrammarSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptGrammar extends MappedText(this)
}

object DescriptionToGrammarSolutionAttempt extends DescriptionToGrammarSolutionAttempt with LongKeyedMetaMapper[DescriptionToGrammarSolutionAttempt] {

}

class GrammarToCNFSolutionAttempt extends LongKeyedMapper[GrammarToCNFSolutionAttempt] with IdPK {
	def getSingleton = GrammarToCNFSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptGrammar extends MappedText(this)
}

object GrammarToCNFSolutionAttempt extends GrammarToCNFSolutionAttempt with LongKeyedMetaMapper[GrammarToCNFSolutionAttempt] {

}

class PumpingLemmaSolutionAttempt extends LongKeyedMapper[PumpingLemmaSolutionAttempt] with IdPK {
	def getSingleton = PumpingLemmaSolutionAttempt

	object solutionAttemptId extends MappedLongForeignKey(this, SolutionAttempt)
	object attemptPL extends MappedText(this)
}

object PumpingLemmaSolutionAttempt extends PumpingLemmaSolutionAttempt with LongKeyedMetaMapper[PumpingLemmaSolutionAttempt] {

}