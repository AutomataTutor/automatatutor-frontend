package com.automatatutor.model

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Failure
import net.liftweb.common.Full
import net.liftweb.mapper._
import net.liftweb.util.SecurityHelpers

class Course extends LongKeyedMapper[Course] with IdPK {
	def getSingleton = Course

	protected object name extends MappedString(this, 300)
	protected object contact extends MappedEmail(this, 100)
	protected object firstPosedProblemSet extends MappedLongForeignKey(this, PosedProblemSet)
	protected object password extends MappedString(this, 20)
	
	def getName : String = this.name.is
	def setName(name : String) : Course = this.name(name)
	
	def getContact : String = this.contact.is
	def setContact(contact : String) : Course = this.contact(contact)
	
	def getFirstPosedProblemSet : Box[PosedProblemSet] = this.firstPosedProblemSet.obj
	def setFirstPosedProblemSet ( problemSet : PosedProblemSet ) : Course = this.firstPosedProblemSet(problemSet)
	def setFirstPosedProblemSet ( problemSet : Box[PosedProblemSet] ) : Course = this.firstPosedProblemSet(problemSet)
	
	def getPassword : String = {
	  if (this.password.is == null || this.password.is.equals("")) { this.password(SecurityHelpers.randomString(8)).save }
	  return this.password.is
	}
	def setPassword ( password : String ) : Course = this.password(password)

	def hasEnrolledStudents : Boolean = {
	  val enrollments = this.getEnrolledStudents
	  return !(enrollments._1.isEmpty && enrollments._2.isEmpty)
	}
	def getEnrolledStudents : (Seq[User], Seq[String]) = {
	  val finalEnrollments = Attendance.findAllFinalEnrollments(this)
	  val preliminaryEnrollments = Attendance.findAllPreliminaryEnrollments(this)

	  return (finalEnrollments, preliminaryEnrollments)
	}
	
	def enroll(student : User) = if(!this.isEnrolled(student)) { Attendance.create.setUser(student).setCourse(this).save }
	def dismiss(student : User) = if(this.isEnrolled(student)) { Attendance.deleteByUserAndCourse(student, this) }
	
	def addInstructor(instructor : User) = Supervision.supervise(instructor, this)
	def getInstructors() : Seq[User] = Supervision.findAll(By(Supervision.course, this)).map(_.instructor openOrThrowException "Each supervision must contain a supervisor")
	
	//def getCourseId : String = (this.id.is.toString + this.name.is.replaceAll(" ", "")).padTo(8, "X").toString.substring(0, 9)
	def getCourseId : String = {
	  val fullId : String = this.id.is.toString + this.name.is.replaceAll(" ", "").toUpperCase()
	  if (fullId.length() > 8) {
	    return fullId.substring(0, 9)
	  } else {
	    return fullId.padTo(8, "X").mkString
	  }
	}

	def removeInstructor(instructor : User) = {
	  if(this.getInstructors.size > 1) {  // Only delete the instructor if it was not the last one
		  Supervision.find(By(Supervision.course, this), By(Supervision.instructor, instructor)) match {
          	case Full(supervision : Supervision) => supervision.delete_!
            case _ => {} // Do nothing if the user was not an instructor to begin with
          }
	  }
	}
	
	def isEnrolled(student : User) = !Attendance.findByUserAndCourse(student, this).isEmpty
	
	def getPosedProblemSets : List[PosedProblemSet] = this.firstPosedProblemSet.map(_.getPosedProblemSetList) openOr List()

	def appendPosedProblemSet(posedProblemSet : PosedProblemSet) = {
	  this.getLastPosedProblemSet match {
	    case Full(lastPosedProblemSet) => lastPosedProblemSet.nextPosedProblemSet(posedProblemSet).save
	    case _ => this.firstPosedProblemSet(posedProblemSet).save
	  }
	}

	def removePosedProblemSet(posedProblemSet : PosedProblemSet) {
	  if(this.firstPosedProblemSet.isEmpty) {
	    return
	  }
	  val firstProblemSet = this.firstPosedProblemSet.openOrThrowException("We just checked that this is not the case")
	  if(firstProblemSet == posedProblemSet) {
	    this.firstPosedProblemSet(firstProblemSet.nextPosedProblemSet.obj)
	  } else {
	    firstProblemSet.remove(posedProblemSet)
	  }
	}
	  
	def getLastPosedProblemSet : Box[PosedProblemSet] = {
	  this.firstPosedProblemSet.map(_.getLastPosedProblemSet)
	}
	
	def canBeDeleted : Boolean = !this.hasEnrolledStudents
	def getDeletePreventers : Seq[String] = if(this.hasEnrolledStudents) { return List("Course still has students enrolled") } else { List() }
	override def delete_! : Boolean = {
	  if (!this.canBeDeleted) {
		return false 
	  } else {
	    Supervision.deleteByCourse(this)
	    return super.delete_!
	  }
	}
}

object Course extends Course with LongKeyedMetaMapper[Course] {
	def findById ( id : String ) : Box[Course] = {
	  val courses = this.findAll()
	  val coursesWithId = courses.filter(_.getCourseId.equals(id))
	  return coursesWithId.size match {
	    case 0 => Empty
	    case 1 => Full(coursesWithId.head)
	    case 2 => Failure("Multiple courses with same id")
	  }
	}
}

class Supervision extends LongKeyedMapper[Supervision] with IdPK {
	def getSingleton = Supervision
			
	object course extends MappedLongForeignKey(this, Course)
	object instructor extends MappedLongForeignKey(this, User)
}

object Supervision extends Supervision with LongKeyedMetaMapper[Supervision] {
	def supervise(instructor : User, course : Course) : Unit = Supervision.create.course(course).instructor(instructor).save
	def findByCourse( course : Course ) : Seq[Supervision] = Supervision.findAll(By(Supervision.course, course))
	def deleteByCourse( course : Course ) : Unit = Supervision.findByCourse(course).map(_.delete_!)
}