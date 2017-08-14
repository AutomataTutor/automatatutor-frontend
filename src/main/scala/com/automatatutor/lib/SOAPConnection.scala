package com.automatatutor.lib

import java.net.HttpURLConnection
import java.net.URL
import scala.xml.Elem
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.Null
import scala.xml.TopScope
import scala.xml.Node
import scala.xml.UnprefixedAttribute
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import scala.xml.XML
import com.automatatutor.model.User

class SOAPConnection(val url : URL) {
    def wrapSOAPEnvelope(body : NodeSeq) : NodeSeq = {
      <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    	<soap:Body> { body } </soap:Body>
      </soap:Envelope>
    }
    
	def callMethod(namespace : String, methodName : String,  arguments : Map[String,Node]) : NodeSeq = {
	  def buildConnection : HttpURLConnection = {
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      
      val methodNameWithNamespace = namespace + "/" + methodName
      
      connection.setDoOutput(true)
      connection.addRequestProperty("Content-Type", "text/xml; charset=utf-8")
      connection.addRequestProperty("SOAPAction", '"' + methodNameWithNamespace + '"')
      
      return connection
	  }
	  
	  def buildRequestBody = {
      def convertArgumentsToXml = {
        def buildXmlNode(label:String, children: NodeSeq) = {
          val prefix = null
          val attributes = Null
          val scope = TopScope
          val minimizeEmpty = true

          Elem(prefix, label, attributes, scope, true, children : _*)
        }

        def buildXmlForSoapArgument(argName: String, argVal: NodeSeq) = buildXmlNode(argName, argVal)

        arguments.map({ case (argName, argVal) => buildXmlForSoapArgument(argName, argVal) } ).toSeq
      }

      def wrapInSoapBody(payload: Seq[Node]) = {
        val prefix = null
        val xmlnsAttribute = new UnprefixedAttribute("xmlns", namespace + '/', Null)
        val scope = TopScope
        val minimizeEmpty = true

        Elem(prefix, methodName, xmlnsAttribute, scope, minimizeEmpty, payload : _*)
      }
      wrapInSoapBody(convertArgumentsToXml)
	  }

	  val connection = buildConnection

	  def buildRequest = {
      val soapBody = buildRequestBody
      val requestXml = wrapSOAPEnvelope(soapBody)
      requestXml.toString
	  }
	  val requestRaw = buildRequest
	  
	  connection.getOutputStream().write(requestRaw.getBytes())

    def responseIsOk = connection.getResponseCode() != HttpURLConnection.HTTP_OK
    def getReturnAsString = scala.io.Source.fromInputStream(connection.getInputStream()).mkString
    def getErrorAsString = scala.io.Source.fromInputStream(connection.getErrorStream()).mkString
	  
	  try {
      if(responseIsOk) {
        return NodeSeq.Empty
      } else {
        def stripWrappingFromResponse(response : NodeSeq) = {
          // There are four levels of wrapping around the result: "soap:Envelope", "soap:Body", "Response", "Result")
          response \ "_" \ "_" \ "_" \ "_"
        }
        

        val returnRaw = getReturnAsString
        val returnWithXmlWrapping = XML.loadString(returnRaw)
        stripWrappingFromResponse(returnWithXmlWrapping)
      }
	  } catch {
	    case exception : Exception => Text(getErrorAsString)
	  }
	}
}

object GraderConnection {
	val serverUrlString = Config.grader.url.get
	val serverUrl = new URL(serverUrlString)
	val soapConnection = new SOAPConnection(serverUrl)
	
	val namespace = Config.grader.methodnamespace.get
	
	// DFA
	
	def getDfaFeedback(correctDfaDescription : String, attemptDfaDescription : String, maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](
	      "dfaCorrectDesc" -> XML.loadString(correctDfaDescription),
	      "dfaAttemptDesc" -> XML.loadString(attemptDfaDescription),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)),
	      "feedbackLevel" -> Elem(null, "feedbackLevel", Null, TopScope, true, Text("Hint")),
	      "enabledFeedbacks" -> Elem(null, "enabledFeedbacks", Null, TopScope, true, Text("ignored")));
	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeFeedbackXML", arguments)
	  
	  return ((responseXml \ "grade").text.toInt, (responseXml \ "feedString" \ "ul" \ "li"))
	}

  //Product Construction

  def getProductConstructionFeedback(correctDfaDescriptionList : List[String], attemptDfaDescription : String, booleanOperation : String, maxGrade : Int) : (Int, NodeSeq) = {

    def stringListToNodeList(xs: List[String]): List[Node] = xs match{
      case Nil => List()
      case y :: ys => Elem(null, "dfaDesc", Null, TopScope, true, XML.loadString(y)) :: stringListToNodeList(ys)
    }

    val arguments = Map[String, Node](
      "dfaDescList" -> Elem(null, "dfaDescList", Null, TopScope, true, stringListToNodeList(correctDfaDescriptionList):_*),
      "dfaAttemptDesc" -> XML.loadString(attemptDfaDescription),
      "booleanOperation" -> Elem(null, "booleanOperation", Null, TopScope, true, Text(booleanOperation)),
      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)),
      "feedbackLevel" -> Elem(null, "feedbackLevel", Null, TopScope, true, Text("Hint")),
      "enabledFeedbacks" -> Elem(null, "enabledFeedbacks", Null, TopScope, true, Text("ignored")))

    val responseXml = soapConnection.callMethod(namespace, "ComputeFeedbackProductConstruction", arguments)

    ((responseXml \ "grade").text.toInt, (responseXml \ "feedString" \ "ul" \ "li"))
  }

  // Minimization

  //TODO: Adjust to work for Minimization
  def getMinimizationFeedback(dfaDescription : String, attemptDfaDescription : String, maxGrade : Int) : (Int, NodeSeq) = {

    val arguments = Map[String, Node](
      "dfaDesc" -> XML.loadString(dfaDescription),
      "dfaAttemptDesc" -> XML.loadString(attemptDfaDescription),
      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)),
      "feedbackLevel" -> Elem(null, "feedbackLevel", Null, TopScope, true, Text("Hint")),
      "enabledFeedbacks" -> Elem(null, "enabledFeedbacks", Null, TopScope, true, Text("ignored")));

    //TODO: Implement 'ComputeFeedbackMinimization' in Backend
    val responseXml = soapConnection.callMethod(namespace, "ComputeFeedbackMinimization", arguments)

    ((responseXml \ "grade").text.toInt, (responseXml \ "feedString" \ "ul" \ "li"))
  }
	
	// NFA 
	
	def getNfaFeedback(correctNfaDescription : String, attemptNfaDescription : String, maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](		   
	      "nfaCorrectDesc" -> XML.loadString(correctNfaDescription),
	      "nfaAttemptDesc" -> XML.loadString(attemptNfaDescription),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)),		  
	      "feedbackLevel" -> Elem(null, "feedbackLevel", Null, TopScope, true, Text("Hint")),
	      "enabledFeedbacks" -> Elem(null, "enabledFeedbacks", Null, TopScope, true, Text("ignored")),
		  "userId" -> Elem(null, "userId", Null, TopScope, true, Text(User.currentUserIdInt.toString))
		  );
	  	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeFeedbackNFAXML", arguments)
	  
	  return ((responseXml \ "grade").text.toInt, (responseXml \ "feedString" \ "ul" \ "li"))
	}
	
	// NFA to DFA
	
	def getNfaToDfaFeedback(correctNfaDescription : String, attemptDfaDescription : String, maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](
	      "nfaCorrectDesc" -> XML.loadString(correctNfaDescription),
	      "dfaAttemptDesc" -> XML.loadString(attemptDfaDescription),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)));
	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeFeedbackNfaToDfa", arguments)
	  
	  return ((responseXml \ "grade").text.toInt, (responseXml \ "feedString" \ "ul" \ "li"))
	}	
	
	// Regular expressions
	
	def getRegexFeedback(correctRegex : String, attemptRegex : String, alphabet : Seq[String], maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](
	      "regexCorrectDesc" -> <div> { correctRegex } </div>,
	      "regexAttemptDesc" -> <div> { attemptRegex } </div>,
	      "alphabet" -> <div> { alphabet.map((symbol : String) => Elem(null, "symbol", Null, TopScope, true, Text(symbol))) } </div>,
	      "feedbackLevel" -> Elem(null, "feedbackLevel", Null, TopScope, true, Text("Hint")),
	      "enabledFeedbacks" -> Elem(null, "enabledFeedbacks", Null, TopScope, true, Text("ignored")),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)));
	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeFeedbackRegexp", arguments)
	  
	  return ((responseXml \ "grade").head.text.toInt, (responseXml \ "feedback"))
	}
	
	def getRegexParsingErrors(potentialRegex : String, alphabet : Seq[String]) : Seq[String] = {
	  val arguments = Map[String, Node](
	      "regexDesc" -> <div> { potentialRegex } </div>,
	      "alphabet" -> <div> { alphabet.map((symbol : String) => Elem(null, "symbol", Null, TopScope, true, Text(symbol))) } </div>)
	      
	  val responseXml = soapConnection.callMethod(namespace, "CheckRegexp", arguments)
	  
	  if(responseXml.text.equals("CorrectRegex")) return List() else return List(responseXml.text)
	}
	
	//Grammar
	def getGrammarParsingErrors(potentialGrammar : String) : Seq[String] = {
	  val arguments = Map[String, Node](
	      "grammar" -> Elem(null, "grammar", Null, TopScope, true, Text(potentialGrammar))
	  )
	      
	  val responseXml = soapConnection.callMethod(namespace, "CheckGrammar", arguments)
	  
	  if(responseXml.text.equals("CorrectGrammar")) return List() else return List(responseXml.text)
	}
	
	def getCNFParsingErrors(potentialGrammar : String) : Seq[String] = {
	  val arguments = Map[String, Node](
	      "grammar" -> Elem(null, "grammar", Null, TopScope, true, Text(potentialGrammar))
	  )
	      
	  val responseXml = soapConnection.callMethod(namespace, "isCNF", arguments)
	  
	  if((responseXml \ "res").head.text.equals("y")) return List() else return List((responseXml \ "feedback").head.text)
	}
	
	def getWordsInGrammarFeedback(grammar: String, wordsIn : Seq[String], wordsOut : Seq[String], maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](
	      "grammar" -> Elem(null, "grammar", Null, TopScope, true, Text(grammar)),
		  "wordsIn" -> <div> { wordsIn.map((symbol : String) => Elem(null, "word", Null, TopScope, true, Text(symbol))) } </div>,
		  "wordsOut" -> <div> { wordsOut.map((symbol : String) => Elem(null, "word", Null, TopScope, true, Text(symbol))) } </div>,
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString))
	  );
	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeWordsInGrammarFeedback", arguments)
	  
	  return ((responseXml \ "grade").head.text.toInt, (responseXml \ "feedback"))
	}
	
	def getDescriptionToGrammarFeedback(solution: String, attempt: String, maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](
	      "solution" -> Elem(null, "solution", Null, TopScope, true, Text(solution)),
		  "attempt" -> Elem(null, "attempt", Null, TopScope, true, Text(attempt)),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)),
	      "checkEmptyWord" -> Elem(null, "checkEmptyWord", Null, TopScope, true, Text(true.toString))
	  );
	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeGrammarEqualityFeedback", arguments)
	  
	  return ((responseXml \ "grade").head.text.toInt, (responseXml \ "feedback"))
	}
	
	def getGrammarToCNFFeedback(solution: String, attempt: String, maxGrade : Int) : (Int, NodeSeq) = {
	  //check that attempt is in CNF
	  val arguments1 = Map[String, Node](
	      "grammar" -> Elem(null, "grammar", Null, TopScope, true, Text(attempt))
	  );
	  val responseXml1 = soapConnection.callMethod(namespace, "isCNF", arguments1)
	  if ((responseXml1 \ "res").head.text == "n") return (-1, (responseXml1 \ "feedback"))
	
	  val arguments2 = Map[String, Node](
	      "solution" -> Elem(null, "solution", Null, TopScope, true, Text(solution)),
		  "attempt" -> Elem(null, "attempt", Null, TopScope, true, Text(attempt)),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString)),
	      "checkEmptyWord" -> Elem(null, "checkEmptyWord", Null, TopScope, true, Text(false.toString))
	  );
	  val responseXml2 = soapConnection.callMethod(namespace, "ComputeGrammarEqualityFeedback", arguments2)
	  return ((responseXml2 \ "grade").head.text.toInt, (responseXml2 \ "feedback"))
	}
	
	def getCYKFeedback(grammar: String, word: String, cyk_attempt: String, maxGrade : Int) : (Int, NodeSeq) = {
	  val arguments = Map[String, Node](
	      "grammar" -> Elem(null, "grammar", Null, TopScope, true, Text(grammar)),
		  "word" -> Elem(null, "word", Null, TopScope, true, Text(word)),
		  "attempt" -> XML.loadString(cyk_attempt),
	      "maxGrade" -> Elem(null, "maxGrade", Null, TopScope, true, Text(maxGrade.toString))
	  );
	  
	  val responseXml = soapConnection.callMethod(namespace, "ComputeCYKFeedback", arguments)
	  
	  return ((responseXml \ "grade").head.text.toInt, (responseXml \ "feedback"))
	}
	
	// Pumping lemma
	
  def getPLParsingErrors(languageDesc : String, constraintDesc : String, 
                      alphabet : Seq[String], pumpingString : String) : Seq[String] = {
    val arguments = Map[String, Node](
        "languageDesc"   -> <div> { languageDesc } </div>,
        "constraintDesc" -> <div> { constraintDesc } </div>,
        "alphabet"       -> <div> { alphabet.map((symbol : String) => Elem(null, "symbol", Null, TopScope, true, Text(symbol))) } </div>,
        "pumpingString"  -> <div> { pumpingString } </div>    
    )	
        
    val responseXml = soapConnection.callMethod(namespace, "CheckArithLanguageDescription", arguments)
    
    if (responseXml.text.equals("CorrectLanguageDescription")) return List() else return List(responseXml.text)
  }
  
  def getPLSplits(languageDesc : String, constraintDesc : String, 
                      alphabet : Seq[String], pumpingString : String) : NodeSeq = {
    val arguments = Map[String, Node](
        "languageDesc"   -> <div> { languageDesc } </div>,
        "constraintDesc" -> <div> { constraintDesc } </div>,
        "alphabet"       -> <div> { alphabet.map((symbol : String) => Elem(null, "symbol", Null, TopScope, true, Text(symbol))) } </div>,
        "pumpingString"  -> <div> { pumpingString } </div>    
    ) 
        
    return soapConnection.callMethod(namespace, "GenerateStringSplits", arguments)
    
    //if (responseXml.text.equals("CorrectLanguageDescription")) return List() else return List(responseXml.text)
  }
  
  def getPLFeedback(languageDesc : String, constraintDesc : String, 
                      alphabet : Seq[String], pumpingString : String,
                      pumpingNumbers : Node) : NodeSeq = {
    val arguments = Map[String, Node](
        "languageDesc"   -> <div> { languageDesc } </div>,
        "constraintDesc" -> <div> { constraintDesc } </div>,
        "alphabet"       -> <div> { alphabet.map((symbol : String) => Elem(null, "symbol", Null, TopScope, true, Text(symbol))) } </div>,
        "pumpingString"  -> <div> { pumpingString } </div>,
        "pumpingNumbers" -> {pumpingNumbers}   
    ) 
        
    return soapConnection.callMethod(namespace, "GetPumpingLemmaFeedback", arguments)
    
    //if (responseXml.text.equals("CorrectLanguageDescription")) return List() else return List(responseXml.text)
  }
}