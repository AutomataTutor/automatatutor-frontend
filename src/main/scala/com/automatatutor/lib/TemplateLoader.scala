package com.automatatutor.lib

import net.liftweb.http.Templates
import scala.xml.NodeSeq
import net.liftweb.common.Full
import net.liftweb.common.Empty

object TemplateLoader {
  private class TemplateLocation(loc : String, addLoc : String*) {
    def getPath : List[String] = { List(loc) ++ List(addLoc : _*) }
  }

  private def load(loc : TemplateLocation) : NodeSeq = Templates(loc.getPath) match {
    case Full(template) => template
    case Empty          => NodeSeq.Empty
    case _              => NodeSeq.Empty
  }

  object BuchiSolving {
    def create = load(new TemplateLocation("buchi-solving", "create"))
    def edit = load(new TemplateLocation("buchi-solving", "edit"))
    def solve = load(new TemplateLocation("buchi-solving", "solve"))
  }
}