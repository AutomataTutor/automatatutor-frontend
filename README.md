# First startup

The following section explains how to get the server and the automatatutor
website running for the first time.

## Prerequisites

The website is written in [Scala](http://scala-lang.org/) using the [Lift webframework](http://liftweb.net/).
We use [sbt](http://www.scala-sbt.org/) for the actual build and to manage the dependencies. Since Lift is
distributed as a library for Scala, you just need the following things installed:

- Scala 2.9.2
- SBT 0.13.5

The versions given are known to be working. Later versions should work, but no guarantee is given for this.

## Initial configuration

Before you compile and start the website for the first time, you will need
to configure the initial user and the connection to the grading engine.
This section shows you how.

Whenever we talk of properties in this file, we mean lift-properties,
which are stored in `src/main/resources/props/default.props` in a simple
format of the form `name=value`.

This handling of properties is a feature of Lift. More information about
properties and how they are used in the code can be found
[here]()

### Admin user

In order to promote users to instructors and admins, an admin account is
needed. One admin account is created on the first startup in order to
create other admins. In the standard configuration, its email and password
are set to `admin@automatatutor.com` and `admin` respectively. Also,
since every user needs a name, the admin's name is set to "Donald Knuth"
initially. These values may be changed *before* you start up the server in
the next section, but will have *no effect once the server is running*.

To change these values, simply edit the properties `admin.firstname`,
`admin.lastname`, `admin.email` and `admin.password`.
If some of these values are not set, they default to the "Admin", "Admin",
`admin@automatatutor.com` and `admin`, respectively.

### Connection to grading engine

Since we communicate with the backend, i.e., the grading engine, via HTTP,
we need to tell the frontend where to find the backend. The configuration
of this connection is stored as properties as well. The property of interest to
you is `grader.url`. Set this to the `.asmx` page that contains the backend-
webservice.

The property `grader.methodnamespace` can remain set to
`http://automatagrader.com` as long as you do not change the namespace in the
backend. This is needed for building the post-request to the backend correctly.

After you have set this property correctly, you can continue to build the frontend

### Mailer

Automatatutor at some points sends out mail to users, for example to verify
email addresses or to send information about solutions that were handed in.
In order to do so, it needs to connect to a mailserver.
This mailserver must be configured using the properties `mail.transport.protocol`,
`mail.smtp.host`, `mail.smtp.port`, mail.smtp.starttls.enable`, `mail.smtp.auth`,
`mail.user`, and `mail.password`.

### Database connection

Automatatutor crucially requires a relational database to run.
The connection to this database must be configured using the properties
`db.driver`,`db.url`, `db.user`, and `db.password`.

## Compile

We assume that you are in the folder AutomataApp_v2. Start sbt as follows

	$ sbt

On the first start, this will take a while as sbt downloads all dependencies
of the project, including the complete lift framework on startup. When sbt
is done, it should greet you with a prompt. Type

	> compile

to compile the frontend into java class files. Once this is done, type

	> container:start

to package the newly compiled class files into a .war-file, together with
all their dependencies and deploy these to a [jetty](http://www.eclipse.org/jetty/)
-server, which sbt starts on its own. Once sbt returns, you should have the
frontend up and running at `http://localhost:8080`. You can stop the frontend
by typing

	> container:stop

# How to Contribute

This section explains the ideas that went into the design of the frontend,
including the actual code. As the frontend is written in Scala and Lift, it
is assumed that you have at least a passing familiarity with functional
programming and may even have looked into Scala a little bit. I try
to make this as accessible as possible by explaining some peculiarities of
Scala and Lift as we go along. I will mark these paragraphs and sections with *Scala* and
*Lift*, respectively, so if you already know one or both of these, you may
want to skip these parts

If you want to learn more about Scala or Lift, I personally found
[Scala by Example][scalabyexample] and [Exploring Lift][exploringlift] to be
very useful.

[scalabyexample]: http://www.scala-lang.org/docu/files/ScalaByExample.pdf (Scala by Example)
[exploringlift]: http://exploring.liftweb.net/ (Exploring Lift)

A reference for Scala and Lift in the versions we use can be found [here][scalaref]
for scala and [here][liftref] for lift

[scalaref]: http://www.scala-lang.org/api/2.9.2/index.html#package (Scala Reference v2.9.2)
[liftref]: http://liftweb.net/api/25/api/ (Lift Reference v2.5)

## *Lift*'s Request Handling

There are three main components involved in rendering a site request:
The sitemap, a website template and a so-called snippet. When a browser issues
a request for, say, the site at /courses/index, the following happens:

1. 	Lift checks the sitemap if there is a site at /courses/index and if it may
be served. The sitemap is a global configuration that is set in
`src/main/scala/bootstrap/liftweb/Boot.scala` like this:

		Menu.i("Courses") / "courses" / "index" >> loggedInPredicate

	`loggedInPredicate` is defined a couple of lines further up as

		val loggedInPredicate = If(() => User.loggedIn_?, () => RedirectResponse("/index"))

	The first line tells Lift that there is a site titled "Courses" at `courses/index`,
which has an additional `LocParam`  called `loggedInPredicate`. The combination of site
and its title is called a `Loc`. The `LocParam` tells Lift that this page may
only be accessed if there is a user logged in. If no user is logged in, then
instead of rendering a site, lift should redirect the user to `/index`
	We just assume that a user is logged in, so lift is satisfied and proceeds
to the second step.

2. Lift looks for a view template called `/courses/index.html` in `src/main/webapp/`.
This is a simple website with some extra tags that control how Lift processes
it before it serves it. In this case, we have 

		<div id="main" class="lift:surround?with=default;at=content">,
		<lift:Courses.showall>

	and

		<lift:Courses.renderenrollmentform form="POST">

	The first tag tells lift that before serving the website, it should first
load `/templates-hidden/default.html` and substitute the `content` tag it finds
in there with the content of `/courses/index`. The other two tags tell lift to
find a Snippet called `Courses` and use the methods `showall` and
`renderenrollmentform` respectively to figure out what to put there.

3. Lift finds the snippet `code.snippet.Courses` and calls its method `showall`
to produce some XML that replaces the tag `<lift:Courses.showall>`. This method
gets passed the children of the `<lift:Courses.showall>` tag, which in this case
is the empty `NodeSeq`.

	The whole work of querying the database and communicating happens in these
snippets. As usual with web frameworks, as much content as possible should
be put in the template and as little as possible should be generated
dynamically.

	*Scala*. Scala offers native support for XML, including the parsing of XML
literals, XPath support and checking the well-formedness of XML at compile-time.
A `Node` represents a single XML-node with its children, a `NodeSeq` is a
sequence of XML nodes. More information on this can be found
[here](http://bcomposes.wordpress.com/2012/05/04/basic-xml-processing-with-scala/)
and [here](http://www.codecommit.com/blog/scala/working-with-scalas-xml-support)

## Code Design

When rendering a page in a snippet, we often query the database for problems,
users and many other things. A very simple, early, design featured only 
snippets (in `code.snippet`) and data access objects (DAOs, called mappers in
Lift, in `com.automatatutor.model`). The snippet would query the DAO for the
objects it needed for displaying and then transform these objects into XML.

This combined two responsibilities in the snippet: Loading the objects needed
from the database and rendering them as needed. In order to promote separation
of concerns, I decided to move these functionalities to two different classes:
A snippet and a renderer. A snippet now only queries the DAOs as needed and
hands all objects to a renderer, which produces a `NodeSeq` from the given
objects.

Since this is a quite large refactoring, as of revision 935, the functionality
is still spread out between snippets and renderers. Also, even though the
renderers should eventually have their own package, they are currently placed
in `code.snippet` as well.

## Problem Snippets

One major design goal was to make sure that new types of problems can be
included as easily as possible. For this, we introduced the notion of a
`ProblemType` (to be found in `model/Problem.scala`). An example of a problem
type would be "English to DFA", i.e., the type of all problems in which the
student has to construct a DFA from a description in English.

`ProblemType` has a method onStartup that asserts that all known problem types
are present in the database. This method is not called by Lift automatically on
startup, but we manually call it in `Boot` (to be found in
`bootstrap/Boot.scala`). After startup, we do not change the known problem types
anymore. This entails that we cannot add a new problem type at runtime, but
have to restart/redeploy the frontend every time we add a new problem type.

`ProblemType` has a method `getProblemSnippet`, which returns a `ProblemSnippet`.
This is a trait (defined in `snippet/ProblemSnippet`) that defines the main
operations that can be performed with problems: They can be created, edited,
solved and deleted. You might have noticed that three of these operations take
a `Problem`. This class encapsulates the properties that all problems have in
common, most importantly a short and a long description as well as a
`ProblemType`.

Whenever the frontend wants to perform an operation on a problem, it loads the
`Problem`, gets its `ProblemSnippet` via its `ProblemType` and then calls the
relevant action on the `ProblemSnippet`. The `ProblemSnippet` is then
responsible for carrying out these actions.


### Hands-On: How to add a new type of problem

Adding a new type of problem is quite simple in general, as far as the frontend
is concerned:

1. Define an object with the trait `ProblemSnippet` that implements all of
its methods. Please refer to the scaladoc-documentation of these methods for more
information on the implementation.
2. Think of a short title for your new problem type and add an entry that maps
this title to your newly created object to `knownProblemTypes` in `ProblemType`
in `model/Problem.scala`.
3. Restart the frontend. This will cause `ProblemType.onStartup` to be called
again, which writes your new problem type to the database and makes it known
to the rest of the frontend.

The challenge in adding a new problem type lies in the correct implementation
of `ProblemSnippet`. This new implementation has to take care of both rendering
the website for the user to create, edit and solve problems, as well as handling
the user's responses, i.e., interpreting the user's input, storing it in the
database and grading students' attempts.

For an example how to implement `ProblemSnippet` correctly,
`RegExConstructionSnippet` (to be found in `snippet/RegExProblemSnippet.scala`)
may be a good starting point, since the frontend consists mainly of a single
text field in which the user enters a regular expression.

## Coding Guidelines

These rules are the basic guidelines that emerged during the first writing of
the code. Since some of these only became clear after a substantial part of
the code was already written, not all of the code adheres to these guidelines.
They are to be thought of as guidelines for new code. Also, they should not be
taken as gospel. If there are good reasons to deviate from these rules, please
feel free to do so.

1. For each class in the model there should be a corresponding renderer that
is parametrized with an instance of this class and creates HTML from the model
instance. This renderer can then be used in the snippets once the relevant
model instances have been loaded

2. Do not query the database directly anywhere but in the model class. For
example, if you want to find a user with a given name, do not write

		User.findAll(By(user.firstname, firstname), By(user.lastname, lastname))

	in the snippet, but rather implement a method

		User.findByName(firstname : String, lastname : String) : Seq[User]

	in `com.automatatutor.model.User` that does this. This helps keep the
snippets as clean and small as possible.

*TODO*: Write down more guidelines

# Rationale for using scala 2.10 instead of current 2.11

At the time of writing (Dec 11, 2014), Lift libraries do exist for Scala 2.11,
but they have not been formally released. They only exist in the form of
milestones and/or release candidates. Thus, we opted for the official release
which only works with scala 2.10 instead of 2.11.
