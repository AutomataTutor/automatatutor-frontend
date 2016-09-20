package com.automatatutor.lib

import scala.xml.NodeSeq
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.TheBindParam
import net.liftweb.util.Helpers.BindParam

abstract trait AbstractRenderer { def render( input : NodeSeq ) : NodeSeq }
trait DynamicRenderer extends AbstractRenderer {
  def render( input : NodeSeq ) : NodeSeq
}
trait Renderer extends AbstractRenderer { 
  protected def render : NodeSeq
  def render ( input : NodeSeq ) : NodeSeq = { this.render }
}
class Binding(val target : String, val renderer : AbstractRenderer)

class Binder(context : String, bindings : Binding*) {
  def this(bindings : Binding*) = this("", bindings : _*)
  
  def bind(template : NodeSeq) : NodeSeq = {
    val bindParams : Seq[BindParam]  = bindings map ( { binding =>
        val filteredTemplate = (template \\ (binding.target)).filter( _.prefix == context )
        new TheBindParam(binding.target, binding.renderer.render(filteredTemplate)).asInstanceOf[BindParam]
    } )
    Helpers.bind(context, template, bindParams : _*)
  }
}