package com.automatatutor.lib

import scala.xml.XML
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq

import org.specs2.Specification

class BinderTest extends Specification { def is = s2"""
  An empty Binder shall
    not change the empty NodeSeq  ${
      (new Binder().bind(NodeSeq.Empty)) must beEqualTo(NodeSeq.Empty)
    }
    replace elements by elements correctly ${
      val binding = new Binding("test", new Renderer { def render = <result></result> })
      new Binder("test", binding).bind(<test:test></test:test>).contains(<result></result>) must beTrue
    }
    handle templates correctly ${
      val binding = new Binding("testTag", new DynamicRenderer { def render(template : NodeSeq) = { <b>{ template.text }</b> } })
      new Binder("testNamespace", binding).bind(<testNamespace:testTag>testText</testNamespace:testTag>).contains(<b>testText</b>) must beTrue
    }
    handle empty data sets correctly ${
      val data = Seq[Int]()
      val renderer = new DataRenderer(data) { def render(template : NodeSeq, data : Int) = { <b>{ template.text + data.toString() }</b> } }
      val binding = new Binding("testTag", renderer)
      val expected = <p/>
      val obtained = new Binder("testNamespace", binding).bind(<p><testNamespace:testTag>value</testNamespace:testTag></p>)
      obtained.mkString.equals(expected.mkString) must beTrue
    }
    handle singleton data sets correctly ${
      val data = Seq(42)
      val renderer = new DataRenderer(data) { def render(template : NodeSeq, data : Int) = { <b>{ template.text + data.toString() }</b> } }
      val binding = new Binding("testTag", renderer)
      val expected = <p><b>value42</b></p>
      val obtained = new Binder("testNamespace", binding).bind(<p><testNamespace:testTag>value</testNamespace:testTag></p>)
      obtained.mkString.equals(expected.mkString) must beTrue
    }
    handle more than three data items correctly ${
      val data = Seq(4,8,15,16,23,42)
      val renderer = new DataRenderer(data) { def render(template : NodeSeq, data : Int) = { <b>{ template.text + data.toString() }</b> } }
      val binding = new Binding("testTag", renderer)
      val expected = <p><b>value4</b><b>value8</b><b>value15</b><b>value16</b><b>value23</b><b>value42</b></p>
      val obtained = new Binder("testNamespace", binding).bind(<p><testNamespace:testTag>value</testNamespace:testTag></p>)
      obtained.mkString.equals(expected.mkString) must beTrue
    }
    replace elements by NodeSeqs correctly ${
      val binding = new Binding("test", new Renderer { def render = <result></result><resultPrime></resultPrime> })
      new Binder("test", binding).bind(<test:test></test:test>) must beEqualTo(<result></result> ++ <resultPrime></resultPrime>)
    }
    respect namespaces ${
      val binding = new Binding("test", new Renderer { def render = <result></result> })
      new Binder("test", binding).bind(<test:test></test:test><testPrime:test></testPrime:test>) must beEqualTo(<result></result> ++ <testPrime:test></testPrime:test>)
    }

  """
    										  
}