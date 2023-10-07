package com.greebiestudios.test_data_generator.data

import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import akka.NotUsed
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import com.greebiestudios.test_data_generator.actor.RunSystem
import com.greebiestudios.test_data_generator.actor.RootActorSystem
import scala.concurrent.ExecutionContext
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import net.sourceforge.tess4j.util.ImageHelper
import com.recognition.software.jdeskew.ImageDeskew
import com.greebiestudios.test_data_generator.ocr.OCR
import akka.stream.scaladsl.JsonFraming
import akka.protobufv3.internal.Api
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import scala.collection.JavaConverters._
import org.apache.pdfbox.text.PDFTextStripper
import akka.actor.Cancellable
import java.io.InputStream
import ch.qos.logback.classic.Logger
import os.exists


class OCRDataSource extends OCR {
    implicit val system: akka.actor.typed.ActorSystem[RunSystem.SystemRunning] =
    RootActorSystem()
    implicit val ec: ExecutionContext = system.executionContext

    val textStripper = new PDFTextStripper()

    case class OcrString(ocrString: String)

    val calgaryBaseUrl = "https://www.calgary.ca/content/dam/www/cfod/finance/documents/plans-budgets-and-financial-reports/annual-reports/"
    val calgaryAnnual2021   = "annual-report-2021.pdf"

    val pdfToTextFlow: Flow[ByteString, OcrString, NotUsed] = exists()
        Flow[ByteString]
            .map(arr => new ByteArrayInputStream(arr.toArray))
            .log("Input Received")
            .map(is => {
                val document = PDDocument.load(is)
                textStripper.setStartPage(44)
                textStripper.setEndPage(47)
                textStripper.getText(document)
            })
            //.mapConcat(text => text.split("\n").toList)
            .map(ocrString => OcrString(ocrString))


    def getOCRDataSource(url: String, resource: String) = {
        val http = Http()
        val uri: Uri = Uri(url + resource)
        val request = HttpRequest(uri = uri)
        val response = http
          .singleRequest(request)
          .flatMap(resp =>
            resp.entity.dataBytes.runReduce(_ ++ _)        
      )
        Source.future(response)
          .via(pdfToTextFlow)
    }

    def printText() = {
        val apiSourceInfo = new ApiSourceInformation()
        val source = getOCRDataSource(calgaryBaseUrl, calgaryAnnual2021)
          .map(x => x.productElementNames.zip(x.productIterator.map((item) => item.toString())).toMap)
          .via(apiSourceInfo.processRecordFlow("rawOcrText"))
          .runWith(apiSourceInfo.kafkaSink)
        source
    }

  
}
