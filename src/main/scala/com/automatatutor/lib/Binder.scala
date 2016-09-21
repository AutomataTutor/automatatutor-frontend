package com.automatatutor.lib

import scala.xml.NodeSeq
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers.TheBindParam
import net.liftweb.util.Helpers.BindParam

/**
 * At several points during processing snippets, we need to create NodeSeqs to plug into templates.
 * These NodeSeqs are usually created ad hoc, querying whatever data they need and plugging them in using net.liftweb.util.Helpers.bind.
 * In order to move away from this ad-hoc-fashion, we implement this Binding-framework
 * that modularizes the individual parts of this task in the hope that we may divide the different concerns when rendering such NodeSeqs.
 * 
 * There are three central parts to this framework: Renderers, Bindings, and the Binder.
 * The goal of the user is most often to create a Binder, on which they can then call the bind(...) method in our to obtain the template with the relevant parts replaced
 * Each Binder contains several Bindings telling it a) which elements to replace, and b) what to replace them with
 * One such pair of (what to replace) and (with what) is encapsulated in a Binding.
 * Each Binding contains a Renderer that tells the Binding what to replace the given location with.
 * 
 * For more detail, please refer to the documentation of the individual classes.
 */

abstract trait AbstractRenderer { 
  /** Called by the Binding when it needs to produce a NodeSeq */
  def render( input : NodeSeq ) : NodeSeq
}

/**
 * The most general kind of renderer.
 * Is given the NodeSeq it shall replace.
 */
trait DynamicRenderer extends AbstractRenderer {
  def render( input : NodeSeq ) : NodeSeq
}

/**
 * A static renderer that only returns a constant NodeSeq.
 * Useful, for example, to produce labels or buttons with constant label.
 * Should be used very rarely, however, since there should be as much freedom
 * as possible in the templates, so nearly every Renderer should react at
 * least to some parameters given in the template.
 */
trait Renderer extends AbstractRenderer { 
  protected def render : NodeSeq
  def render ( input : NodeSeq ) : NodeSeq = { this.render }
}

/**
 * Quite often it is necessary to render some data for the user.
 * This usually amounts to binding certain information of the data to the
 * template and doing so for each element of data found in the database.
 * This Renderer encapsulates this process by defining an abstract method
 * for implementation that is called for every data object in the given sequence.
 * 
 * Can be used, for example, like
 *     new DataRenderer(Seq(1,2,3)) { override def render ( template : NodeSeq, data : Int ) : NodeSeq { <b> data </b> } }
 * which would return the NodeSeq <b>1</b><b>2</b><b>3</b>, while ignoring the template
 */
abstract class DataRenderer[T](data : Seq[T]) extends AbstractRenderer {
  protected def render ( template : NodeSeq, data : T ) : NodeSeq
  def render( template : NodeSeq ) : NodeSeq = data.flatMap { data => render(template, data) }
}

/**
 * Encapsulates the "where" and the "what" of binding content to a template.
 * In order to bind the result of `renderer` to the tag `target`, define a Binding like
 *     new Binding("target", renderer)
 */
class Binding(val target : String, val renderer : AbstractRenderer)

/**
 * Encapsulates several bindings and binds them all to all tags that match that binding with a given prefix.
 * For example,
 *     new Binder("testNs", new Binding("target", renderer)).bind(<testNs:target></testNs:target>)
 * would return the result of renderer, while
 *     new Binder("otherNs", new Binding("target", renderer)).bind(<testNs:target></testNs:target>)
 * would return <testNs:target></testNs:target>.
 */
class Binder(prefix : String, bindings : Binding*) {
  def this(bindings : Binding*) = this("", bindings : _*)
  
  def bind(template : NodeSeq) : NodeSeq = {
    val bindParams : Seq[BindParam]  = bindings map ( { binding =>
      val filteredTemplate = (template \\ binding.target).filter( _.prefix == prefix )
      new TheBindParam(binding.target, binding.renderer.render(filteredTemplate)).asInstanceOf[BindParam]
    } )
    Helpers.bind(prefix, template, bindParams : _*)
  }
}