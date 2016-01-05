package com.automatatutor.renderer

import com.automatatutor.SpecificationWithExamplesInsideBootedLiftSession
import com.automatatutor.model.User
import com.automatatutor.model.Course
import org.specs2.specification.Step

class UserRendererTest extends SpecificationWithExamplesInsideBootedLiftSession("") { def is = s2"""
  For an enrolled user		${ Step(enrolledCourse.enroll(user))}
	UserRenderer.renderDeleteLink should return a link	${renderer.renderDeleteLink.head.label must beEqualTo("a") }
	that points to /users/index	${(renderer.renderDeleteLink \ "@href").text must startWith("/users/index") }
  """
	isolated
  
  lazy val user = { val user = User.create.firstName("Thomas").lastName("Andersen"); user.save(); user }
  lazy val enrolledCourse = { val course = Course.create; course.save(); course }
  lazy val supervisedCourse = { val course = Course.create; course.save(); course }
  lazy val renderer = new UserRenderer(user);
  
}