package com.automatatutor.lib

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
      System.out.println(new Binder("testNamespace", binding).bind(<testNamespace:testTag>testText</testNamespace:testTag>))
      new Binder("testNamespace", binding).bind(<testNamespace:testTag>testText</testNamespace:testTag>).contains(<b>testText</b>) must beTrue
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