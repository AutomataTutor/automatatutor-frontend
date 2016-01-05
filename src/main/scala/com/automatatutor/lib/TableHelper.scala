package com.automatatutor.lib

import scala.xml.{NodeSeq, Text, Elem, Null, TopScope, Node}

object TableHelper {
	private def renderTableHeader( headings : Seq[String] ) : Node = {
	  val headingsXml : NodeSeq = headings.map(heading => <th> { heading } </th>)
	  return <tr> { headingsXml } </tr>
	}
	 
	private def renderSingleRow[T] ( datum : T , displayFuncs : Seq[T => NodeSeq]) : Node = {
	  return <tr> { displayFuncs.map(func => <td> { func(datum) } </td>) } </tr>
	}

	private def renderTableBody[T] ( data : Seq[T], displayFuncs : Seq[T => NodeSeq]) : NodeSeq = {
	  return data.map(renderSingleRow(_, displayFuncs))
	}
	
	def renderTable[T] (data : Seq[T], displayFuncs : (T => NodeSeq)*) : NodeSeq = {
	  val dataRows = renderTableBody(data, displayFuncs)

	  return <table> { dataRows } </table>
	}
	
	def renderTableWithHeader[T] (data : Seq[T], colSpec : (String, (T => NodeSeq))*) : NodeSeq = {
	  val headings = colSpec.map(_._1)
	  val headerRow = renderTableHeader(headings)

	  val displayFuncs = colSpec.map(_._2)
	  val dataRows = renderTableBody(data, displayFuncs)

	  return <table> { headerRow ++ dataRows } </table>
	}
}
