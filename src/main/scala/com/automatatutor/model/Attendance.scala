package com.automatatutor.model

import net.liftweb.mapper._
import net.liftweb.common.Box

class Attendance extends LongKeyedMapper[Attendance] with IdPK {
	def getSingleton = Attendance

	protected object userId extends MappedLongForeignKey(this, User)
	protected object courseId extends MappedLongForeignKey(this, Course)
	protected object email extends MappedEmail(this, 100)
	
	def getUser : User = this.userId.obj openOrThrowException "Every Attendance must have a User"
	def setUser ( user : User ) = this.userId(user)
	
	def getCourse : Box[Course] = this.courseId.obj
	def setCourse ( course : Course ) = this.courseId(course)
	def setCourse ( course: Box[Course] ) = this.courseId(course)
	
	def getEmail = this.email.get
	def setEmail ( email : String ) = this.email(email)
}

object Attendance extends Attendance with LongKeyedMetaMapper[Attendance] {
	def deleteByCourse( course : Course ) = this.bulkDelete_!!(By(Attendance.courseId, course))
	def finalizePreliminaryEnrollments ( user : User ) = this.findAllByEmail(user.email.is).map(attendance => attendance.email(null).userId(user).save)
	def createPreliminaryEnrollment ( course : Course, email : String ) = this.create.courseId(course).email(email).save
	def findAllByEmail ( email : String ) = this.findAll(By(Attendance.email, email))
	def findAllFinalEnrollments ( course : Course ) : Seq[User] = {
	  this.findAll(By(Attendance.courseId, course), NotNullRef(Attendance.userId)).map(_.userId.obj openOrThrowException "Invariant violated: Both Attendance.userId and Attendance.email are null")
	}
	def findAllPreliminaryEnrollments ( course : Course ) : Seq[String] = this.findAll(By(Attendance.courseId, course), NullRef(Attendance.userId)).map(_.email.is)
	
	def findAllByUser(user : User) : List[Attendance] = 
	  this.findAll(By(Attendance.userId, user))
	def deleteAllByUser(user : User) : Unit = 
	  this.bulkDelete_!!(By(Attendance.userId, user))
	
	def findByUserAndCourse(user : User, course : Course) : Box[Attendance] = 
	  this.find(By(Attendance.userId, user), By(Attendance.courseId, course))
	def deleteByUserAndCourse(user : User, course : Course) : Unit = 
	  this.bulkDelete_!!(By(Attendance.userId, user), By(Attendance.courseId, course))

	def findByEmailAndCourse(email : String, course : Course) : Box[Attendance] = 
	  this.find(By(Attendance.email, email), By(Attendance.courseId, course))
	def deleteByUserAndCourse(email : String, course : Course) : Unit = 
	  this.bulkDelete_!!(By(Attendance.email, email), By(Attendance.courseId, course))
}
