package com.automatatutor.snippet

import scala.xml.NodeSeq
import scala.xml.Text
import com.automatatutor.lib.TableHelper
import com.automatatutor.model.Attendance
import com.automatatutor.model.Course
import com.automatatutor.model.DFAConstructionProblem
import com.automatatutor.model.DFAConstructionProblemCategory
import com.automatatutor.model.DFAConstructionSolutionAttempt
import com.automatatutor.model.NFAConstructionProblem
import com.automatatutor.model.NFAConstructionProblemCategory
import com.automatatutor.model.NFAConstructionSolutionAttempt
import com.automatatutor.model.NFAToDFAProblem
import com.automatatutor.model.NFAToDFASolutionAttempt
import com.automatatutor.model.PosedProblem
import com.automatatutor.model.PosedProblemSet
import com.automatatutor.model.Problem
import com.automatatutor.model.ProblemSet
import com.automatatutor.model.ProblemType
import com.automatatutor.model.Role
import com.automatatutor.model.SolutionAttempt
import com.automatatutor.model.Supervision
import com.automatatutor.model.User
import com.automatatutor.renderer.UserRenderer
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.mapper.By
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.http.PaginatorSnippet
import net.liftweb.mapper.StartAt
import net.liftweb.mapper.MaxRows
import net.liftweb.util.Props

object userToEdit extends RequestVar[User](null)

class Users extends PaginatorSnippet[User] {
  override def count = User.findAll().size
  override def itemsPerPage = Props.getInt("layout.usersperpage") openOr 50
  override def page = User.findAll(StartAt(curPage*itemsPerPage), MaxRows(itemsPerPage)) 
  
  def showuser (xhtml : NodeSeq) : NodeSeq = {
    val user = userToEdit.is
    
    def editSubmit() = { 
      user.save
      S.redirectTo("/users/index")
    }
    
    def firstNameField = SHtml.text(user.firstName.is, user.firstName(_))
    def lastNameField = SHtml.text(user.lastName.is, user.lastName(_))
    def emailField = SHtml.text(user.email.is, user.email(_))
    
    def studentRoleLink =
      if(user.hasStudentRole) {
      	SHtml.link("/users/edit", () => { userToEdit(user); user.removeStudentRole }, Text("Remove student role"))
      } else {
      	SHtml.link("/users/edit", () => { userToEdit(user); user.addStudentRole }, Text("Make student"))
      }

    def instructorRoleLink =
      if(user.hasInstructorRole) {
      	SHtml.link("/users/edit", () => { userToEdit(user); user.removeInstructorRole }, Text("Remove instructor role"))
      } else {
      	SHtml.link("/users/edit", () => { userToEdit(user); user.addInstructorRole }, Text("Make instructor"))
      }

    def adminRoleLink =
      if(user.hasAdminRole) {
      	SHtml.link("/users/edit", () => { userToEdit(user); user.removeAdminRole }, Text("Remove admin role"))
      } else {
      	SHtml.link("/users/edit", () => { userToEdit(user); user.addAdminRole }, Text("Make admin"))
      }
    
    def submitButton =
      SHtml.submit("Submit", editSubmit)
    
    Helpers.bind("userdisplay", xhtml,
        "firstname" -> firstNameField,
        "lastname" -> lastNameField,
        "email" -> emailField,
        "studentrolechange" -> studentRoleLink,
        "instructorrolechange" -> instructorRoleLink,
        "adminrolechange" -> adminRoleLink,
        "submitbutton" -> submitButton)
  }
  
  def showall(ignored : NodeSeq) : NodeSeq = {
    val users = page
      
    def userToDeleteLink(user : User) : NodeSeq = {
      def deleteUser(user : User) = {
        user.delete_!
        Attendance.deleteAllByUser(user)
        Supervision.bulkDelete_!!(By(Supervision.instructor, user))
      }
      SHtml.link("/users/index", () => deleteUser(user), Text("Delete user"))
    }
        

    def userToEditLink(user : User) : NodeSeq =
    	SHtml.link("/users/edit", () => userToEdit(user), Text("Edit user"))
    	
    val userTable = TableHelper.renderTableWithHeader(users,
        ("First Name", (user : User) => Text(user.firstName.is)),
        ("Last Name", (user : User) => Text(user.lastName.is)),
        ("Email", (user : User) => Text(user.email.is)),
        ("Roles", (user : User) => Text(Role.findAll(By(Role.userId, user)).mkString(", "))),
        ("", (user : User) => userToEditLink(user)),
        ("", (user : User) => (new UserRenderer(user)).renderDeleteLink))
        
    return userTable
  }
  
  def resetlink(ignored : NodeSeq) : NodeSeq = {
    def resetDatabase() = {
      List(Attendance, Course, PosedProblem, PosedProblemSet, Problem, ProblemType, 
          DFAConstructionProblem, DFAConstructionProblemCategory, NFAConstructionProblem, NFAConstructionProblemCategory,
          NFAToDFAProblem, NFAToDFASolutionAttempt,
          ProblemSet, SolutionAttempt, Supervision, DFAConstructionSolutionAttempt, NFAConstructionSolutionAttempt).map(_.bulkDelete_!!())
    }
    	
    val resetLink = SHtml.link("/users/index", () => resetDatabase, Text("Reset Database"))
    	
    return resetLink
  }

}