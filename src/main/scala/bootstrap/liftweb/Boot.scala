package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import net.liftmodules.JQueryModule
import net.liftweb.http.js.jquery._
import net.liftweb.db.{DB,StandardDBVendor,DefaultConnectionIdentifier}
import net.liftweb.mapper.Schemifier
import com.automatatutor.model._
import java.io.FileInputStream

import net.liftweb.util.{Props, Mailer}
import javax.mail.{Authenticator,PasswordAuthentication}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
  
     Mailer.authenticator = for {
	  user <- Props.get("mail.user")
	  pass <- Props.get("mail.password")
	} yield new Authenticator {	  
	  override def getPasswordAuthentication =
		new PasswordAuthentication(user,pass)
	}
  
    // where to search snippet
	LiftRules.addToPackages("com.automatatutor")

	if(!DB.jndiJdbcConnAvailable_?) {
		val dbDriver = Props.get("db.driver") openOrThrowException "No db-driver specificied"
		val dbUrl = Props.get("db.url") openOrThrowException "No db-path specified"
		val dbUser = Props.get("db.user")
		val dbPassword = Props.get("db.password")
		val vendor = new StandardDBVendor(dbDriver, dbUrl, dbUser, dbPassword)

		LiftRules.unloadHooks.append(vendor.closeAllConnections_!)

		DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
	}

	Schemifier.schemify(true, Schemifier.infoF _, 
	    User, Attendance, Course, PosedProblem, PosedProblemSet, Problem, ProblemType, 
	    DFAConstructionProblem, DFAConstructionProblemCategory, DFAConstructionSolutionAttempt,
      NFAConstructionProblem, NFAConstructionProblemCategory, NFAConstructionSolutionAttempt, 
	    NFAToDFAProblem, NFAToDFASolutionAttempt, 
      RegExConstructionProblem, RegexConstructionSolutionAttempt,
      PumpingLemmaProblem, PumpingLemmaSolutionAttempt,
	    ProblemSet, Role, SolutionAttempt, Supervision)
	
	// Make sure that we have the entries we need in the database
	// If you want to check some invariants about the database at startup,
	// this would be the place to put them
    ProblemType.onStartup
    DFAConstructionProblemCategory.onStartup
    NFAConstructionProblemCategory.onStartup
    User.onStartup

	val loggedInPredicate = If(() => User.loggedIn_?, () => RedirectResponse("/index"))
	val notLoggedInPredicate = If(() => !User.loggedIn_?, () => RedirectResponse("/index"))
	val isInstructorPredicate = If(() => User.currentUser.map(_.hasInstructorRole) openOr false, () => { S.error("This page is only available to instructors"); RedirectResponse("/index") })
	val isAdminPredicate = If(() => User.currentUser.map(_.hasAdminRole) openOr false, () => { S.error("This page is only available to admins"); RedirectResponse("/index") })

    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index",
      
      Menu.i("Try it Now!") / "preview" / "index" >> notLoggedInPredicate submenus(
          Menu.i("Solve Preview") /"preview" / "solve" >> Hidden),
          
      Menu.i("Practice Problem Sets") / "practicesets" / "index" >> loggedInPredicate submenus(
          Menu.i("Solve Practice Set Problem") /"practicesets" / "solve" >> Hidden),
      
      Menu.i("Problems") / "problems" / "index" >> isInstructorPredicate submenus(
          Menu.i("Create Problem") / "problems" / "create" >> Hidden,
          Menu.i("Edit Problem") / "problems" / "edit" >> Hidden),
          
      Menu.i("Problem Sets") / "problemsets" / "index" >> isInstructorPredicate submenus(
          Menu.i("Create Problem Set") / "problemsets" / "create" >> Hidden,
          Menu.i("Edit Problem Set") / "problemsets" / "edit" >> Hidden,
          Menu.i("Append Problem to Problem Set") / "problemsets" / "addproblem" >> Hidden,
          Menu.i("Pose Problem to Problem Set") / "problemsets" / "poseproblem" >> Hidden),

      Menu.i("Courses") / "courses" / "index" >> loggedInPredicate submenus(
          Menu.i("Show Course") / "courses" / "show" >> Hidden,
          Menu.i("Solve Problem Set") / "courses" / "solveproblemset" >> Hidden,
          Menu.i("Solve Practice Problem") / "courses" / "solvepractice" >> Hidden,
          Menu.i("Solve Problem") / "courses" / "solveproblem" >> Hidden,
          Menu.i("Create Course") / "courses" / "create" >> Hidden,
          Menu.i("Manage Course") / "courses" / "manage" >> Hidden,
          Menu.i("Choose Problem Set To Add") / "courses" / "chooseproblemset" >> Hidden,
          Menu.i("Pose Problem Set") / "courses" / "poseproblemset" >> Hidden),

      Menu.i("Users") / "users" / "index" >> isAdminPredicate submenus(
    	  Menu.i("Edit User") / "users" / "edit" >> Hidden)

	) ::: User.sitemap ::: List(
	  Menu.i("About") / "about" / "index" submenus(
          Menu.i("Terms of service") / "about" / "terms-of-service", // >> Hidden,
          Menu.i("Privacy statement") / "about" / "privacy-statement") // >> Hidden )
	)

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries : _*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery172
    JQueryModule.init()

  }
}
