package com.automatatutor.model

import net.liftweb.mapper._

class Attendance extends LongKeyedMapper[Attendance] with IdPK {
	def getSingleton = Attendance

	object userId extends MappedLongForeignKey(this, User)
	object courseId extends MappedLongForeignKey(this, Course)
	object email extends MappedEmail(this, 100)
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
}
