package com.automatatutor.snippet

import java.text.DateFormat
import java.util.Calendar
import java.util.Date

import scala.Array.canBuildFrom
import scala.xml.NodeSeq
import scala.xml.Text

import com.automatatutor.lib.DownloadHelper
import com.automatatutor.lib.TableHelper
import com.automatatutor.model.Attendance
import com.automatatutor.model.Course
import com.automatatutor.model.PosedProblem
import com.automatatutor.model.PosedProblemSet
import com.automatatutor.model.Problem
import com.automatatutor.model.ProblemSet
import com.automatatutor.model.SolutionAttempt
import com.automatatutor.model.Supervision
import com.automatatutor.model.User
import com.automatatutor.renderer.CourseRenderer

import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.RequestVar
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.mapper.By
import net.liftweb.util.AnyVar.whatVarIs
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.bind
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.util.SecurityHelpers

/**
  * Created by Jan on 08.08.2017.
  */
class MinimizationTable {

  def showall(ignored : NodeSeq) : NodeSeq = {

    val attendedCourses = User.currentUser.map(_.getAttendedCourses) openOr List()
    val attendedCoursesNodeSeq = if (!attendedCourses.isEmpty) {
      <h2> Attended Courses </h2> ++ NodeSeq.Empty
    } else {
      <h2> Där är inte sa manga kurser! </h2> ++ NodeSeq.Empty
    }

    val supervisedCourses = User.currentUser.map(_.getSupervisedCourses) openOr List()
    val supervisedCoursesNodeSeq = if (!supervisedCourses.isEmpty) {
      <h2> Supervised Courses </h2> ++ NodeSeq.Empty
    } else {
      NodeSeq.Empty
    }

    val currentUserIsInstructor = User.currentUser.map(_.hasInstructorRole) openOr false
    val createCourseLink = if(currentUserIsInstructor) {
      SHtml.link("/courses/create", () => (), Text("Create new Course"))
    } else {
      NodeSeq.Empty
    }

    return attendedCoursesNodeSeq ++ supervisedCoursesNodeSeq ++ createCourseLink
  }

}
