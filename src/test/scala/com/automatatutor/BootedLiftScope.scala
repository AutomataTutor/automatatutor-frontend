package com.automatatutor

import org.specs2.Specification
import org.specs2.execute.Result
import org.specs2.execute.Result
import org.specs2.mutable.Around
import org.specs2.mutable.Before
import org.specs2.specification.Example
import org.specs2.specification.Example
import org.specs2.specification.Fragment
import org.specs2.specification.Fragment
import org.specs2.specification.Fragments
import org.specs2.specification.Fragments
import org.specs2.specification.Scope
import org.specs2.specification.Scope
import net.liftweb.http.provider.HTTPSession
import net.liftweb.mockweb.MockWeb
import net.liftweb.http.S
import net.liftweb.http.LiftSession
import net.liftweb.util.StringHelpers
import net.liftweb.common.Empty
import org.specs2.specification.Step
import net.liftweb.common.Box
import net.liftweb.mocks.MockHttpServletRequest
import net.liftweb.mocks.MockHttpServletRequest


object LiftBootHelper {
  private var booted = false

  def bootLift = {
    if (!booted) {
      import bootstrap.liftweb.Boot
      val boot = new Boot
      boot.boot
      booted = true
    }
  }
}

abstract class SpecificationWithExamplesInsideBootedLiftSession(url : String) extends Specification {
  override def map(fs : => Fragments) : Fragments = Step(LiftBootHelper.bootLift) ^ fs.map(executeBodyInSession)
  
  private var session : Box[LiftSession] = Empty
  private val fullUrl = "http://automatatutor.com" + url
  val mockReq = new MockHttpServletRequest("http://foo.com/test/this?foo=bar", "/test")

  private def executeInsideSession[T](t : => T) = {
    LiftBootHelper.synchronized( {
        MockWeb.testS(mockReq, session) { val retVal = t; session = S.session; retVal }
    } )
  }

  private def executeBodyInSession(fragment : Fragment) : Fragment = fragment match {
      case (example : Example) => Example(example.desc, { this.executeInsideSession(example.body()) })
      case (step : Step) => Step(this.executeInsideSession(step.execute))
      case notAnExample => notAnExample
  }
}