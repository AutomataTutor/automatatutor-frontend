package com.automatatutor.model

import net.liftweb.mapper._
import net.liftweb.common.Full
import java.util.Calendar
import scala.xml.Elem
import scala.xml.UnprefixedAttribute
import scala.xml.Null
import scala.xml.TopScope
import scala.xml.NodeSeq
import scala.xml.Node
import scala.xml.Text
import net.liftweb.common.Empty
import java.util.Date
import net.liftweb.common.Box

class PosedProblemSet extends LongKeyedMapper[PosedProblemSet] with IdPK {
	override def getSingleton = PosedProblemSet

	protected object problemSetId extends MappedLongForeignKey(this, ProblemSet)
	protected object startDate extends MappedDateTime(this)
	protected object endDate extends MappedDateTime(this)
	protected object nextPosedProblemSet extends MappedLongForeignKey(this, PosedProblemSet)
	protected object useRandomOrder extends MappedBoolean(this)
	
	def getProblemSet : ProblemSet = this.problemSetId.obj openOrThrowException "Every PosedProblemSet must have ProblemSet"
	def setProblemSet ( problemSet : ProblemSet ) = this.problemSetId(problemSet)
	
	def getStartDate : Date = this.startDate.is
	def setStartDate ( startDate : Date ) = this.startDate(startDate)
	
	def getEndDate : Date = this.endDate.is
	def setEndDate ( endDate : Date ) = this.endDate(endDate)
	
	def getNextPosedProblemSet : Box[PosedProblemSet] = this.nextPosedProblemSet.obj
	def setNextPosedProblemSet ( nextPosedProblemSet : PosedProblemSet ) = this.nextPosedProblemSet(nextPosedProblemSet)
	def setNextPosedProblemSet ( nextPosedProblemSet : Box[PosedProblemSet] ) = this.nextPosedProblemSet(nextPosedProblemSet)
	
	def getUseRandomOrder = this.useRandomOrder.is
	def setUseRandomOrder ( useRandomOrder : Boolean ) = this.useRandomOrder(useRandomOrder)
	
	/** A problem set is considered active if the current time is after the start date and the problem set is not expired */
	def isActive : Boolean = {
	  val now = Calendar.getInstance().getTime()
	  val startDate = this.startDate.is
	  val endDate = this.endDate.is
	  return (now.equals(startDate) || now.after(startDate)) && now.before(endDate)
	}

	def isExpired : Boolean = {
	  val now = Calendar.getInstance().getTime()
	  val endDate = this.endDate.is
	  return (now.equals(endDate) || now.after(endDate))
	}
	
	def isRandomlyOrdered : Boolean = {
	  return this.useRandomOrder.is
	}
	
	def getCourse : Course = {
	  val allCourses = Course.findAll().filter(_.getPosedProblemSets.contains(this))
	  if(allCourses.size == 1) {
	    return allCourses(0)
	  } else {
	    return null
	  }
	}
	
	private def getGradesForStudent ( student : User ) : Seq[Int] = this.getPosedProblems.map(_.getGrade(student, this))
	
	private def getAllStudentsWithGrades : Seq[(User, Seq[Int])] = this.getCourse.getEnrolledStudents._1.map(student => (student, getGradesForStudent(student)))

	def renderGradesCsv : String = {
	  val studentsGrades = getAllStudentsWithGrades
	  val csvLines = studentsGrades.map(tuple => List(tuple._1.firstName, tuple._1.lastName, tuple._1.email, tuple._2.mkString(";")).mkString(";"))
	  return csvLines.mkString("\n")
	}
	
	def renderGradesXml : Node = {
	  val studentsGrades = getAllStudentsWithGrades
	  val maxGrades = this.getProblemSet.getPosedProblems.map(_.getMaxGrade)
	  val userGrades = studentsGrades.map(tuple => {
	   val student : User = tuple._1
	   val gradesWithMaxGrades : Seq[(Int, Long)] = tuple._2 zip maxGrades
	   val userEmailAttribute = new UnprefixedAttribute("email", student.email.is, Null)
	   val userLastNameAttribute = new UnprefixedAttribute("lastname", student.lastName.is, userEmailAttribute)
	   val userFirstNameAttribute = new UnprefixedAttribute("firstname", student.firstName.is, userLastNameAttribute)
	   
	   val children : NodeSeq = gradesWithMaxGrades.map(gradeVal => {
	     val grade = gradeVal._1
	     val maxGrade = gradeVal._2
	     Elem(null, "grade", new UnprefixedAttribute("maxgrade", maxGrade.toString, Null), TopScope, true, Text(grade.toString))
	   })
	   
	   new Elem(null, "usergrades", userFirstNameAttribute, TopScope, true, children : _*)
	  })
	  return <problemsetgrades> { userGrades } </problemsetgrades>
	}
	
	def getPosedProblemSetList : List[PosedProblemSet] = List(this) ::: (this.nextPosedProblemSet.map(_.getPosedProblemSetList) openOr List())
	def getLastPosedProblemSet : PosedProblemSet = nextPosedProblemSet.map(_.getLastPosedProblemSet) openOr this
	
	def getPosedProblems : Seq[PosedProblem] = {
	  return this.problemSetId.obj.map(_.getPosedProblems) openOrThrowException "Every posed problem set must have a problem set"
	}
	
	def getPosedProblemsInOrder ( inRandomOrder : Boolean , seed : Int ) : Seq[PosedProblem] = {
	  return this.problemSetId.obj.map(_.getPosedProblemsInOrder(inRandomOrder, seed)) openOrThrowException "Every posed problem set must have a problem set"
	}
	
	def getNumberOpenProblems ( user : User ) : Int = {
	  return this.getPosedProblems.filter(_.isOpen(user, this)).size
	}
	
	def getGrade( user : User ) : Long = {
	  return this.getGradesForStudent(user).sum
	}
	
	def getMaxGrade : Long = {
	  return this.getPosedProblems.map(_.getMaxGrade).sum
	}
	
	def getTimeToExpirationInMs : Long = {
	  val nowTimestamp = Calendar.getInstance().getTime().getTime()
	  val endDateTimestamp = this.endDate.is.getTime()
	  return endDateTimestamp - nowTimestamp
	}
	
	def canBeRemoved : Boolean = !this.isActive
	def getRemovePreventers : Seq[String] = if(this.isActive) { List("Problem Set is currently active") } else { List() }
	def remove(toRemove : PosedProblemSet) : Unit = {
	  this.nextPosedProblemSet.obj match {
	    case Full(nextPosedSet) => if (nextPosedSet == toRemove) {
	    		this.nextPosedProblemSet(nextPosedSet.nextPosedProblemSet.obj).save
	    		nextPosedSet.delete_!
	    	} else {
	    		nextPosedSet.remove(toRemove)
	    	}
	    case _ => () // We reached the end of the list, do nothing
	  }
	}
	
	def deleteRecursively : Unit = {
	  this.nextPosedProblemSet.obj.map(_.deleteRecursively)
	  this.delete_!
	}
}

object PosedProblemSet extends PosedProblemSet with LongKeyedMetaMapper[PosedProblemSet] {
	def existsForProblemSet(problemSet : ProblemSet) : Boolean = this.find(By(PosedProblemSet.problemSetId, problemSet)) match { 
	case Empty => false 
	case Full(_) => true
	case _ => false
}
	def findByProblemSet(problemSet : ProblemSet) : Seq[PosedProblemSet] = this.findAll(By(PosedProblemSet.problemSetId, problemSet))
}
