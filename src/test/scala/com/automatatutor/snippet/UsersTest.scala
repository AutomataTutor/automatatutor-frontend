package com.automatatutor.snippet

import com.automatatutor.SpecificationWithExamplesInsideBootedLiftSession
import com.automatatutor.model.User
import org.specs2.specification.Step
import scala.xml.NodeSeq
import com.automatatutor.LiftBootHelper
import scala.xml.Node

class ShowAllTest extends SpecificationWithExamplesInsideBootedLiftSession("") { def is = s2"""
	When an admin is logged in, the Users.showall-method must 			${ Step( { init; User.logUserIn(adminUser) }) }
			return a single element	${ getShowallResult() must haveSize(1) }
			which is a table	${ getShowallResult()(0).label must be("table") }
			with five rows (four users and one header) ${ (getShowallResult()(0) \\ "tr") must haveSize(5) }
  """
  
  lazy val adminUser = { 
    val user = User.create.firstName("Alan").lastName("Turing").email("alan.turing@berkeley.edu");
    user.addAdminRole; user.save; user
  }

  lazy val instructorUser = { 
    val user = User.create.firstName("Ada").lastName("Lovelace").email("ada.lovelace@berkeley.edu");
    user.addInstructorRole; user.save; user
  }

  lazy val studentUser = { 
    val user = User.create.firstName("Alan").lastName("Cox").email("alan.cox@berkeley.edu");
    user.addStudentRole; user.save; user
  }

  def init = { adminUser; instructorUser; studentUser }
  
  def getShowallResult() = new Users().showall(NodeSeq.Empty)
  
  def cleanup = { adminUser.delete_!; instructorUser.delete_!; studentUser.delete_! }
}