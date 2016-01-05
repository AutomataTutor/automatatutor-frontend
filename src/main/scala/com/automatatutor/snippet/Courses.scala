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

object CourseReqVar extends RequestVar[Course](null)
object PosedProblemSetReqVar extends RequestVar[PosedProblemSet](null)
object PosedProblemReqVar extends RequestVar[PosedProblem](null)
object ProblemSetReqVar extends RequestVar[ProblemSet](null)


class Courses {
  def showall(ignored : NodeSeq) : NodeSeq = {

    val attendedCourses = User.currentUser.map(_.getAttendedCourses) openOr List()
    val attendedCoursesNodeSeq = if (!attendedCourses.isEmpty) {
      <h2> Attended Courses </h2> ++ { displayAttendedCourses(attendedCourses) }
    } else {
      NodeSeq.Empty
    }
    
    val supervisedCourses = User.currentUser.map(_.getSupervisedCourses) openOr List()
    val supervisedCoursesNodeSeq = if (!supervisedCourses.isEmpty) {
      <h2> Supervised Courses </h2> ++ { displaySupervisedCourses(supervisedCourses) }
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

  def renderSolvePracticeSet( practiceSet : ProblemSet ) : NodeSeq = {
    val posedProblems = practiceSet.getPosedProblems
    
    return TableHelper.renderTableWithHeader(posedProblems, 
        ("Name", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.shortDescription.is)),
        ("Type", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.getTypeName)),
        ("", (posedProblem : PosedProblem) => SHtml.link("/courses/solvepractice", () => { PosedProblemReqVar(posedProblem) }, Text("solve"))))
  }
  
  def displayAttendedCourses(courses : Seq[Course]) : NodeSeq = {
    return TableHelper.renderTableWithHeader(courses,
        ("Course Name", (course : Course) => Text(course.name.is)),
        ("Contact", (course : Course) => new CourseRenderer(course).renderContactLink),
        ("", (course : Course) => new CourseRenderer(course).renderShowLink))
  }
  
  def displaySupervisedCourses(courses : Seq[Course]) : NodeSeq = {
    return TableHelper.renderTableWithHeader(courses, 
        ("Course Name", (course : Course) => Text(course.name.is)),
        ("Contact", (course : Course) => new CourseRenderer(course).renderContactLink),
        ("", (course : Course) => new CourseRenderer(course).renderManageLink),
        ("", (course : Course) => new CourseRenderer(course).renderDeleteLink))
  }
  
  def rendershow ( xhtml : NodeSeq ) : NodeSeq = {
    val course : Course = CourseReqVar.is

    val courseName : String = course.name.is
    
    val problemSets = course.getPosedProblemSets

    val user : User = User.currentUser openOrThrowException "Lift only allows logged-in-users here";
    
    def problemSetToSolveLink ( problemSet : PosedProblemSet ) : NodeSeq = {
      val hasOpenProblems = problemSet.getNumberOpenProblems(user) > 0
      if(hasOpenProblems) {
        return SHtml.link("/courses/solveproblemset",
          () => { CourseReqVar(course); PosedProblemSetReqVar(problemSet) },
          Text("Solve"))
      } else {
        return NodeSeq.Empty
      }
    }
    
    def problemSetToExpirationTime ( problemSet : PosedProblemSet ) : NodeSeq = {
      val timeInMs : Long = problemSet.getTimeToExpirationInMs
      
      val msPerSecond = 1000
      val msPerMinute = 60 * msPerSecond
      val msPerHour = 60 * msPerMinute
      val msPerDay = 24 * msPerHour
      
      val days = timeInMs / msPerDay
      val hours = (timeInMs % msPerDay) / msPerHour
      val minutes = (timeInMs % msPerHour) / msPerMinute
      val seconds = (timeInMs % msPerMinute) / msPerSecond
      
      return Text("Expires in " + days + " days, " + hours + ":" + minutes + ":" + seconds + " hours")
    }
    
    def renderActiveProblemSetsTable ( problemSets : Seq[PosedProblemSet] ) : NodeSeq = {
        val problemSetsTable = TableHelper.renderTableWithHeader(problemSets,
            ("Name", (problemSet : PosedProblemSet) => Text(problemSet.getProblemSet.name.is)),
            ("Problems Open", (problemSet : PosedProblemSet) => Text(problemSet.getNumberOpenProblems(user) + " Problems open")),
            ("Expiration", (problemSet : PosedProblemSet) => problemSetToExpirationTime(problemSet)),
            ("Current Grade", (problemSet : PosedProblemSet) => Text("Grade: " + problemSet.getGrade(user) + "/" + problemSet.getMaxGrade)))
            

        val problemSetsHeaderRow = (problemSetsTable \ "tr").head
        val problemSetsDataRows : NodeSeq = (problemSetsTable \ "tr").tail
        val problemSetsWithRows = problemSets.zip(problemSetsDataRows)
        val problemSetsWithProblemsRows = problemSetsWithRows.flatMap(tuple => {
        	val posedProblemSet = tuple._1
        	val problemSetRow = tuple._2
        	val problemsTable = renderSolveProblemSet(course, posedProblemSet)
        	problemSetRow ++ <tr> <td colspan="4"> { problemsTable } </td> </tr>
        })

        return <table> { problemSetsHeaderRow } { problemSetsWithProblemsRows } </table>
    }
    
    val activeProblemSets = problemSets.filter(_.isActive)
    val activeProblemSetsTable = renderActiveProblemSetsTable(activeProblemSets)
    
    val expiredProblemSets = problemSets.filter(_.isExpired)
    
    val expiredProblemSetsTable = TableHelper.renderTableWithHeader(expiredProblemSets,
        ("Name", (problemSet : PosedProblemSet) => Text(problemSet.getProblemSet.name.is)),
        ("Expired On", (problemSet : PosedProblemSet) => Text(problemSet.endDate.toString) ),
        ("Final Grade", (problemSet : PosedProblemSet) => Text(problemSet.getGrade(user) + "/" + problemSet.getMaxGrade)))
    
    Helpers.bind("showcourse", xhtml,
        "name" -> courseName,
        "activesets" -> activeProblemSetsTable,
        "expiredsets" -> expiredProblemSetsTable)
  }
  
  def rendermanage(xhtml : NodeSeq) : NodeSeq = {
    val course = CourseReqVar.is

    val courseName = course.name.is
    val courseNameField = SHtml.text(courseName, course.name(_))
    
    val contactMail = course.contact.is
    val contactMailField = SHtml.text(contactMail, course.contact(_))
    
    val submitButton = SHtml.submit("Submit", () => { course.save; S.redirectTo("/courses/index") })
    
    bind("courseinfo", xhtml,
        "name" -> courseNameField,
        "contact" -> contactMailField,
        "submitbutton" -> submitButton)
  }
  
  def renderposedproblemsets(xhtml : NodeSeq) : NodeSeq = {
    val course = CourseReqVar.is
    val posedProblemSets = course.getPosedProblemSets
    
    val posedProblemSetsNodeSeq = if (posedProblemSets.isEmpty) {
      Text("No problem sets posed") ++ <br />
    } else {
      TableHelper.renderTableWithHeader(posedProblemSets,
          ("Name", (posedProblemSet : PosedProblemSet) => Text(posedProblemSet.getProblemSet.name.is)),
          ("Start Date", (posedProblemSet : PosedProblemSet) => Text(posedProblemSet.startDate.toString)),
          ("End Date", (posedProblemSet : PosedProblemSet) => Text(posedProblemSet.endDate.toString)),
          ("Download Grades", (posedProblemSet : PosedProblemSet) => 
            (DownloadHelper.renderCsvDownloadLink(posedProblemSet.renderGradesCsv, "grades", Text("csv")) ++ Text(" ") ++
             DownloadHelper.renderXmlDownloadLink(posedProblemSet.renderGradesXml, "grades", Text("xml")))
          ),
          ("", (posedProblemSet : PosedProblemSet) => new CourseRenderer(course).renderRemoveProblemSetLink(posedProblemSet)))
    }
    
    val poseProblemSetLink = SHtml.link("/courses/chooseproblemset", () => CourseReqVar(course), Text("Pose new problem set")) ++ <br /> ++ <br />
    return posedProblemSetsNodeSeq ++ poseProblemSetLink
  }
  
  def renderenrollment(xhtml : NodeSeq) : NodeSeq = {
    def enrollStudents(course : Course, emails : String) : Unit = {
      def enrollSingleStudent(course : Course, email : String) : Unit = {
        User.findByEmail(email) match {
          case Full(user) => course.enroll(user)
          case _ => Attendance.createPreliminaryEnrollment(course, email)
        }
      }

      val parsedEmails : Array[String] = emails.split("[ \t\n]").map(_.trim).filter(!_.isEmpty())
      parsedEmails.map(enrollSingleStudent(course, _))
    }
    
    def dismissRegisteredStudent(course : Course, student : User) = {
      course.dismiss(student);
      S.notice(student.email.is + " successfully dismissed")

      S.redirectTo("/courses/manage", () => CourseReqVar(course))
    }
    
    def dismissUnregisteredStudent(course : Course, email : String) = {
      Attendance.find(By(Attendance.courseId, course), By(Attendance.email, email)) match {
        case Full(attendance) => attendance.delete_!
        case _ => // Do nothing
      }
    }

    val course = CourseReqVar.is

    val enrollments = course.getEnrolledStudents
    val finalEnrollments = enrollments._1
    val preliminaryEnrollments = enrollments._2
    
    val enrolledStudentsTable = TableHelper.renderTableWithHeader(finalEnrollments,
        ("First Name", (student : User) => Text(student.firstName.is)),
        ("Last Name", (student : User) => Text(student.lastName.is)),
        ("Email", (student : User) => Text(student.email.is)))
        
    val courseId = course.getCourseId
    val coursePassword = course.getPassword
    
    bind("enrollment", xhtml,
    	"enrolled-students" -> enrolledStudentsTable,
    	"courseid" -> courseId,
    	"coursepassword" -> coursePassword)
  }
  
  def rendercreate(xhtml : NodeSeq) : NodeSeq = {
    var name : String = ""

    def createCourse() = {
      if (name.isEmpty()) {
        S.error("Name must not be empty")
        S.redirectTo("/courses/index")
      } else {
        val email = User.currentUser.map(_.email.is) openOr ""

        val course: Course = Course.create.name(name).contact(email).password(SecurityHelpers.randomString(8))
        course.save

        Supervision.supervise(User.currentUser openOrThrowException "Lift prevents non-logged-in users from being here", course)

        S.redirectTo("/courses/index")
      }

    }

    val defaultName = ""

    val nameField = SHtml.text(defaultName, name = _, "maxlength" -> "300")
    val submitButton = SHtml.submit("Create", createCourse)

    bind("formfield", xhtml,
      "name" -> nameField,
      "createbutton" -> submitButton)
  }
  
  def renderproblemsets ( ignored : NodeSeq ) : NodeSeq = {
    val course : Course = CourseReqVar.is
    val problemSets : Seq[ProblemSet] = ProblemSet.getByCreator(User.currentUser openOrThrowException "Lift prevents non-logged-in users from being here")

    return TableHelper.renderTableWithHeader(problemSets,
        ("Name", (problemSet : ProblemSet) => Text(problemSet.name.is)),
        ("Number of Problems", (problemSet : ProblemSet) => Text("Contains " + problemSet.getNumberOfProblems + " problems")),
        ("Name", (problemSet : ProblemSet) => SHtml.link("/courses/poseproblemset", () => { CourseReqVar(course); ProblemSetReqVar(problemSet) }, Text("Pose problem set"))))
  }
  
  def renderposeproblemset ( xhtml : NodeSeq ) : NodeSeq = {
    val course : Course = CourseReqVar.is
    val problemSet : ProblemSet = ProblemSetReqVar.is
    
    val dateFormat : DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    val now : Calendar = Calendar.getInstance()
    val oneWeekFromNow : Calendar = Calendar.getInstance();
    oneWeekFromNow.add(Calendar.WEEK_OF_YEAR, 1);
    
    var startDateString : String = dateFormat.format(now.getTime())
    var endDateString : String = dateFormat.format(oneWeekFromNow.getTime())
    var inRandomOrder : Boolean = false; 
	 
    def poseProblemSet() = {
      val errors : List[String] = List()
      val startDate : Date = try { 
        dateFormat.parse(startDateString)
      } catch {
        case e : Exception => { 
          errors ++ List(e.getMessage())
          null
        }
      }
	  if (startDate == null) {		
		S.redirectTo("/courses/poseproblemset", () => { CourseReqVar(course); ProblemSetReqVar(problemSet) } )		
	  } else { 
      	  
		  val endDate : Date = try { 
			dateFormat.parse(endDateString)
		  } catch {
			case e : Exception => { 
			  errors ++ List(e.getMessage())
			  null
			}
		  }
		  
		  if (endDate == null) {			
			S.redirectTo("/courses/poseproblemset", () => { CourseReqVar(course); ProblemSetReqVar(problemSet) } )			
		  } else {
			val posedProblemSet = 
			   PosedProblemSet.create.startDate(startDate).endDate(endDate).problemSetId(problemSet).useRandomOrder(inRandomOrder)
			posedProblemSet.save
			course.appendPosedProblemSet(posedProblemSet)
			S.redirectTo("/courses/manage", () => CourseReqVar(course))
		  }
		  
		  /* else {
			errors.map(S.error(_))
			S.redirectTo("/courses/poseproblemset", () => { CourseReqVar(course); ProblemSetReqVar(problemSet) } )
		  }*/
	  }
    }
    
	
    val startDateField = SHtml.text(startDateString, startDateString = _, 
			"id" -> "startdate", "readonly" -> "readonly")
    val endDateField = SHtml.text(endDateString, endDateString = _, 
			"id" -> "enddate", "readonly" -> "readonly")
	val randomOrderField = SHtml.checkbox_id(false, inRandomOrder = _, 
                  Full("randomorderfield"))
	
    val poseButton = SHtml.submit("Pose Problem", poseProblemSet)
    
    Helpers.bind("poseproblemsetform", xhtml,
        "startdatefield" -> startDateField,
        "enddatefield" -> endDateField,
		"randomorderfield" -> randomOrderField,
        "posebutton" -> poseButton)
  }
  
  def rendersolveproblemset ( ignored : NodeSeq ) : NodeSeq = {
    val user = User.currentUser openOrThrowException "Lift only allows logged in users on here"
    val course = CourseReqVar.is
    val posedProblemSet = PosedProblemSetReqVar.is

    return renderSolveProblemSet(course, posedProblemSet)
  }
  
  def renderSolveProblemSet( course : Course, posedProblemSet : PosedProblemSet ) : NodeSeq = {
    val user = User.currentUser openOrThrowException "Lift only allows logged in users on here"	
	
	var posedProblems = posedProblemSet.getPosedProblemsInOrder(
							posedProblemSet.isRandomlyOrdered, User.currentUserIdInt)


    def renderGrade ( problem : PosedProblem ) : NodeSeq = {
      val numberAttempts = problem.getNumberAttempts(user, posedProblemSet)
      val numberRemainingAttempts = problem.getNumberAttemptsRemaining(user, posedProblemSet)
      if (numberAttempts == 0) {
        return Text("Not yet attempted")
      } else {
        val grade = problem.getGrade(user, posedProblemSet)
        val maxGrade = problem.getMaxGrade
        val gradeString : String = grade + "/" + maxGrade
        if (problem.isOpen(user, posedProblemSet)) {
        	return Text("Best Grade so far: " + gradeString)
        } else {
        	return Text("Final grade: " + gradeString)
        }
      }
    }
    
    def renderAttemptsRemaining ( problem : PosedProblem ) : NodeSeq = {
      if (problem.isPracticeProblem) {
    	  Text("Practice Problem")
      } else {
    	  Text(problem.getNumberAttemptsRemaining(user, posedProblemSet) + " attempts remaining")
      }
    }
    
    def renderSolveLink ( problem : PosedProblem ) : NodeSeq = {
    	if(problem.isOpen(user, posedProblemSet)) {
    		return SHtml.link("/courses/solveproblem",
    	      () => { CourseReqVar(course); 
				PosedProblemSetReqVar(posedProblemSet); 
				PosedProblemReqVar(problem) },
    	      Text("solve"))
    	} else {
    		return NodeSeq.Empty
    	}
    }
    
    return TableHelper.renderTableWithHeader(posedProblems,
        ("Description", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.shortDescription.is)),
        ("Problem Type", (posedProblem : PosedProblem) => Text(posedProblem.getProblem.getTypeName)),
        ("Attempts remaining", (posedProblem : PosedProblem) => renderAttemptsRemaining(posedProblem)),
        ("Grade", (posedProblem : PosedProblem) => renderGrade(posedProblem)),
        ("", (posedProblem : PosedProblem) => renderSolveLink(posedProblem)))
  }
  
  def rendersolveproblem ( ignored : NodeSeq ) : NodeSeq = {
    val user = User.currentUser openOrThrowException "Lift only allows logged in users on here"
    val course = CourseReqVar.is
    val posedProblemSet = PosedProblemSetReqVar.is
    val posedProblem = PosedProblemReqVar.is
    
    val problem = posedProblem.getProblem
    val problemSnippet = problem.getType.getProblemSnippet
    
    val lastAttempt = SolutionAttempt.getLatestAttempt(user, posedProblemSet, posedProblem)
    
    var lastGrade = 0
    
    def recordSolutionAttempt(grade : Int, dateTime : Date) : SolutionAttempt = {
      lastGrade = grade
      val solutionAttempt = SolutionAttempt.create.posedProblemSetId(posedProblemSet).posedProblemId(posedProblem).userId(user)
      solutionAttempt.dateTime(dateTime).grade(grade).save
      return solutionAttempt
    }
    
    def returnToCourse() = {
      S.redirectTo("/courses/show", () => { CourseReqVar(course) } )
    }
    
    def remainingAttempts() : Int = {
      posedProblem.getNumberAttemptsRemaining(user, posedProblemSet).toInt
    }
    
    def bestGrade() : Int = {
      posedProblem.getGrade(user, posedProblemSet)
    }

    problemSnippet.renderSolve(problem, posedProblem.maxGrade.is, lastAttempt, recordSolutionAttempt, returnToCourse, remainingAttempts, bestGrade)
  }
  
  def rendersolvepractice ( ignored : NodeSeq ) : NodeSeq = {
    val posedProblem : PosedProblem = PosedProblemReqVar.is
    val problem : Problem = posedProblem.getProblem
    val snippet = problem.getType.getProblemSnippet
    return snippet.renderSolve(posedProblem.getProblem, posedProblem.getMaxGrade, Empty,
        (grade, date) => SolutionAttempt, () => S.redirectTo("/courses/index"), () => 1, () => 0)
  }
  
  def renderenrollmentform ( xhtml : NodeSeq ) : NodeSeq = {
    val user = User.currentUser openOrThrowException "Lift only allows logged in users here"
    var courseId : String = ""
    var coursePassword : String = ""
      
    def enroll() = {
      val courseToEnroll = Course.findById(courseId) match {
        case Full(course) => course
        case _ => { S.error("Course with id " + courseId + " not found"); S.redirectTo("/courses/index") }
      }
      
      if(courseToEnroll.password.equals(coursePassword)) {
        courseToEnroll.enroll(user)
      } else {
        S.error("Password " + coursePassword + " is incorrect for course " + courseId)
      }
      S.redirectTo("/courses/index")
    }
      
    val courseIdField = SHtml.text("", courseId = _)
    val coursePasswordField = SHtml.text("", coursePassword = _)
    val enrollButton = SHtml.submit("Enroll", enroll)
    
    bind("enrollmentform", xhtml,
        "courseidfield" -> courseIdField,
        "coursepasswordfield" -> coursePasswordField,
        "enrollbutton" -> enrollButton)
  }
}