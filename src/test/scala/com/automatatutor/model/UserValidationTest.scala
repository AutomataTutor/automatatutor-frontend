package com.automatatutor.model

import org.specs2.specification.Scope
import net.liftweb.util.Props
import net.liftweb.mockweb.MockWeb
import net.liftweb.http.S
import net.liftweb.common.Failure
import net.liftweb.util.FieldError
import scala.xml.Text
import org.specs2.execute.Result
import com.automatatutor.LiftBootHelper
import org.specs2.specification.Step
import org.specs2.specification.Fragment
import org.specs2.specification.Fragments
import org.specs2.specification.Example
import org.specs2.Specification
import net.liftweb.db.DefaultConnectionIdentifier
import com.automatatutor.SpecificationWithExamplesInsideBootedLiftSession

class UserValidationTest extends SpecificationWithExamplesInsideBootedLiftSession("") { def is = s2"""
  A newly created user should
    have an empty first name    ${ testForEmptyFirstName }
    have an empty last name     ${ testForEmptyLastName }
    have an empty email address ${ testForEmptyEmail }
    have a nonempty password    ${ testForUnemptyPassword }
    have a student role         ${ testForStudentRole }
    have four validation errors ${ user.validate must have size(4) }
    have a validation error set to firstName, stating 'First name must be set' ${ testForFirstNameValidationError }
    have a validation error set to lastName, stating 'Last name must be set'   ${ testForLastNameValidationError }
    have a validation error set to email, stating 'Invalid email address'      ${ testForEmailValidationError }
    have a validation error set to password, stating 'Password must be set'    ${ testForPasswordValidationError }

  After setting the first name to 'John' it should ${ Step(user.firstName("John")) }
    have the first name set to 'John' ${ testForFirstNameEqualTo("John") }
    have an empty last name           ${ testForEmptyLastName }
    have an empty email address       ${ testForEmptyEmail }
    have a nonempty password          ${ testForUnemptyPassword }
    have a student role               ${ testForStudentRole }
    have three validation errors      ${user.validate must have size(3)}
    have a validation error set to lastName, stating 'Last name must be set' ${ testForLastNameValidationError }
    have a validation error set to email, stating 'Invalid email address'    ${ testForEmailValidationError }
    have a validation error set to password, stating 'Password must be set'  ${ testForPasswordValidationError }

  After setting the last name to 'Smith' it should ${ Step(user.lastName("Smith")) }
    have the first name set to 'John' ${ testForFirstNameEqualTo("John")}
    have the last name set to 'Smith' ${ testForLastNameEqualTo("Smith")}
    have an empty email address       ${ testForEmptyEmail }
    have a nonempty password          ${ testForUnemptyPassword }
    have a student role               ${ testForStudentRole }
    have two validation errors        ${ user.validate must have size(2)}
    have a validation error set to email, stating 'Invalid email address'   ${ testForEmailValidationError }
    have a validation error set to password, stating 'Password must be set' ${ testForPasswordValidationError }
    	
  After setting the email address to 'john.smith@berkeley.edu' it should ${ Step(user.email("john.smith@berkeley.edu")) }
    have the first name set to 'John' ${testForFirstNameEqualTo("John")}
    have the last name set to 'Smith' ${testForLastNameEqualTo("Smith")}
    have the email address set to 'john.smith@berkeley.edu'                 ${testForEmailEqualTo("john.smith@berkeley.edu")}
    have a nonempty password          ${ testForUnemptyPassword }
    have a student role               ${ testForStudentRole }
    have one validation error         ${ user.validate must have size(1)}
    have a validation error set to password, stating 'Password must be set' ${ testForPasswordValidationError }
    	
  After setting the password to 'password123' it should ${ Step(user.password("password123")) }
    have the first name set to 'John' ${ testForFirstNameEqualTo("John") }
    have the last name set to 'Smith' ${ testForLastNameEqualTo("Smith") }
    have the email address set to 'john.smith@berkeley.edu'${ testForEmailEqualTo("john.smith@berkeley.edu") }
    have a nonempty password          ${ testForUnemptyPassword }
    match the password 'password123'  ${ testForPasswordMatching("password123") }
    have a student role       ${ testForStudentRole }
    have no validation error  ${ user.validate must have size(0) }
    not attend any courses    ${ (user.attendsCourses must beFalse) and (user.getAttendedCourses must beEmpty) }
    not supervise any courses ${ (user.supervisesCourses must beFalse) and (user.getSupervisedCourses must beEmpty) }
    
    ${ Step({attendedCourse.dismiss(user); supervisedCourse.removeInstructor(user); attendedCourse.delete_!; supervisedCourse.delete_!; user.delete_!} )}
  """
    isolated
    	
  lazy val user = { val user = User.create; user.save; user }
  lazy val attendedCourse = { val course = Course.create; course.save; course }
  lazy val supervisedCourse = { val course = Course.create; course.save; course }
  
  def testForFirstNameEqualTo(firstName : String) = user.firstName.is must beEqualTo(firstName)
  def testForLastNameEqualTo(lastName : String) = user.lastName.is must beEqualTo(lastName)
  def testForEmailEqualTo(email : String) = user.email.is must beEqualTo(email)
  def testForPasswordMatching(password : String) = user.password.match_?(password)
  
  def testForEmptyFirstName = user.firstName.is must beEmpty
  def testForEmptyLastName = user.lastName.is must beEmpty
  def testForEmptyEmail = user.email.is must beEmpty

  def testForUnemptyPassword = user.password.is must not beEmpty

  def testForFirstNameValidationError = user.validate must contain(new FieldError(user.firstName, Text("First name must be set")))
  def testForLastNameValidationError = user.validate must contain(new FieldError(user.lastName, Text("Last name must be set")))
  def testForEmailValidationError = user.validate must contain(new FieldError(user.email, Text("Invalid email address")))
  def testForPasswordValidationError = user.validate must contain(new FieldError(user.password, Text("Password must be set")))
  
  def testForStudentRole = user.hasStudentRole must beTrue
  def testForNoStudentRole = user.hasStudentRole must beFalse
  def testForNoInstructorRole = user.hasInstructorRole must beFalse
  def testForNoAdminRole = user.hasAdminRole must beFalse
  def testForNoRoles = testForNoStudentRole and testForNoInstructorRole and testForNoAdminRole
}
