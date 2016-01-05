package com.automatatutor.model

import org.specs2._
import specification._
import com.automatatutor.SpecificationWithExamplesInsideBootedLiftSession
import com.automatatutor.LiftBootHelper

class CourseAttendanceTest extends SpecificationWithExamplesInsideBootedLiftSession("") { def is = s2"""
  Concerning course enrollment it should      ${ Step(attendedCourse.enroll(user)) }
    attend a course                           ${user.attendsCourses must beTrue}
    attend exactly one course                 ${user.getAttendedCourses must haveSize(1)}
    attend the course it was just enrolled in ${user.getAttendedCourses must contain(attendedCourse)}
    not supervise any courses                 ${(user.supervisesCourses must beFalse) and (user.getSupervisedCourses must beEmpty)}
    										  ${ Step({attendedCourse.dismiss(user); attendedCourse.delete_!; user.delete_!})}
  """
    isolated
    
  lazy val user : User = { val user = User.create; user.save; user }
  lazy val attendedCourse : Course = { val course = Course.create; course.save; course }
}