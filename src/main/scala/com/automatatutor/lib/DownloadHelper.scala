package com.automatatutor.lib

import net.liftweb.http.StreamingResponse
import java.io.ByteArrayInputStream
import net.liftweb.http.ResponseShortcutException
import scala.xml.NodeSeq
import net.liftweb.http.SHtml

abstract class FileType(mimeType : String, fileSuffix : String) {
  def getMimeType = mimeType
  def getFileSuffix = fileSuffix
}
case object CsvFile extends FileType("text/csv", ".csv")
case object XmlFile extends FileType("text/xml", ".xml")
case object XlsxFile extends FileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx")

object DownloadHelper {
  private def offerDownloadToUser( contents : String, filename : String, filetype : FileType ) = {
    val filenameWithSuffix = filename + filetype.getFileSuffix
	val headers = 
	  "Content-type" -> filetype.getMimeType ::
	  "Content-length" -> contents.length().toString ::
	  "Content-disposition" -> ("attachment; filename=" + filenameWithSuffix) :: Nil
	val responseCode = 200
	
	val downloadResponse = new StreamingResponse(new ByteArrayInputStream(contents.getBytes()), () => {}, contents.length, headers, Nil, responseCode)
	throw new ResponseShortcutException(downloadResponse)
  }

  def renderCsvDownloadLink ( contents : String, filename : String, linkBody : NodeSeq ) : NodeSeq = {
    return SHtml.link("ignored", () => offerDownloadToUser(contents, filename, CsvFile), linkBody)
  }
  
  def renderXmlDownloadLink ( contents : NodeSeq, filename : String, linkBody : NodeSeq ) : NodeSeq = {
    return SHtml.link("ignored", () => offerDownloadToUser(contents.toString, filename, XmlFile), linkBody)
  }

  def renderXlsxDownloadLink ( contents : String, filename : String, linkBody : NodeSeq ) : NodeSeq = {
    return SHtml.link("ignored", () => offerDownloadToUser(contents.toString, filename, XlsxFile), linkBody)
  }
}