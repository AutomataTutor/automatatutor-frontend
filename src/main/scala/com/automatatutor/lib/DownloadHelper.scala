package com.automatatutor.lib

import java.io.ByteArrayInputStream

import scala.xml.NodeSeq

import net.liftweb.http.ResponseShortcutException
import net.liftweb.http.SHtml
import net.liftweb.http.StreamingResponse

object DownloadHelper {

  private abstract class FileType(mimeType : String, fileSuffix : String) {
    def getMimeType = mimeType
    def getFileSuffix = fileSuffix
  }
  private case object CsvFile extends FileType("text/csv", ".csv")
  private case object XmlFile extends FileType("text/xml", ".xml")
  private case object XlsxFile extends FileType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx")

  private def offerDownloadToUser(contents: String, filename: String, filetype: FileType) = {
    def buildDownloadResponse = {
      val contentAsBytes = contents.getBytes()
      val downloadSize = contentAsBytes.length

      def buildStream = {
        new ByteArrayInputStream(contentAsBytes)
      }
      
      def buildHeaders = {
        val filenameWithSuffix = filename + filetype.getFileSuffix
        List(
          "Content-type" -> filetype.getMimeType,
          "Content-length" -> downloadSize.toString,
          "Content-disposition" -> ("attachment; filename=" + filenameWithSuffix)
        )
      }

      val stream = buildStream
      val onEndCallback = () => {}
      val headers = buildHeaders
      val cookies = Nil
      val responseCode = 200
      
      new StreamingResponse(
          stream, onEndCallback, downloadSize, headers, cookies, responseCode
      )
    }

    throw new ResponseShortcutException(buildDownloadResponse)
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