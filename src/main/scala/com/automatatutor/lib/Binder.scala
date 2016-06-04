package com.automatatutor.lib

import scala.xml.NodeSeq
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.TheBindParam
import net.liftweb.util.Helpers.BindParam

trait Renderer { def render : NodeSeq }
class Binding(val target : String, val renderer : Renderer)

class Binder(context : String, bindings : Binding*) {
  def this(bindings : Binding*) = this("", bindings : _*)
  
  def bind(template : NodeSeq) = {
    val bindParams : Seq[BindParam]  = bindings map (binding => new TheBindParam(binding.target, binding.renderer.render).asInstanceOf[BindParam])
    Helpers.bind(context, template, bindParams : _*)
  }
}