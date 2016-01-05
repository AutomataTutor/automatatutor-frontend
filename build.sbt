// Taken nearly verbatim from lift_blank example from https://github.com/lift/lift_25_sbt/tarball/master
name := "AutomataTutor"

version := "0.0.1"

organization := "com.automatatutor"

scalaVersion := "2.10.4"

fork in Keys.test := true

parallelExecution in Keys.test := false

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  Seq(
    "net.liftweb"             %% "lift-webkit"            % "2.5.1"               % "compile",
    "net.liftweb"             %% "lift-mapper"            % "2.5.1"               % "compile",
    "net.liftweb"             %% "lift-testkit"           % "2.5.1"               % "compile",
    "net.liftmodules"         %% "lift-jquery-module_2.5" % "2.4",
    "org.eclipse.jetty"       % "jetty-webapp"            % "9.2.5.v20141112"     % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet"           % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          % "logback-classic"         % "1.1.2",
    "org.specs2"              %% "specs2-core"            % "2.4.15"              % "test",
	"org.postgresql" 		  % "postgresql" 			  % "9.3-1102-jdbc41",
	"com.h2database" 	      % "h2" 					  % "1.4.182"			  % "container,test",
	"org.seleniumhq.selenium" % "selenium-java"           % "2.44.0"              % "test"
  )
}
