package com.greebiestudios.test_data_generator.data

import akka.actor._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.Http
import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.model.{DateTime, HttpRequest, HttpResponse}
import java.util.concurrent.ExecutorService
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.duration.Duration
import com.greebiestudios.test_data_generator.data.Producers
import akka.kafka.scaladsl.{Consumer, Producer}
import com.typesafe.config.ConfigFactory
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.ProducerRecord
import com.greebiestudios.test_data_generator.data.Marshallers.CkanResource
import akka.util.ByteString
import akka.stream.scaladsl.Flow
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.impl.PushNotUsed
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import akka.stream.scaladsl.StreamConverters
import java.io.File
import collection.JavaConverters._
import java.io.InputStream
import collection.convert.ImplicitConversions
import org.slf4j.LoggerFactory
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.Sheet
import akka.http.scaladsl.marshalling.Marshal
import com.greebiestudios.test_data_generator.data.CkanDataCaseClasses

class ApiSourceInformation
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
  import CsvParsing._
  val supportedFormats = Seq("XLSX", "XLS", "JSON", "CSV", "XML")
  val logger = LoggerFactory.getLogger(classOf[ApiSourceInformation])
  val configFile = ConfigFactory.load("application.conf")
  val config = configFile.getConfig("akka.kafka.producer")
  val bootstrapServers =
    configFile.getString("akka.kafka.producer.kafka-clients.bootstrap.servers")
  val delimiter: Byte = Comma
  val quoteChar: Byte = DoubleQuote
  val escapeChar: Byte = Backslash

  // Sources


  // Flows

  val csvFlow: Flow[ByteString, List[ByteString], NotUsed] =
    CsvParsing.lineScanner(delimiter, quoteChar, escapeChar)

  val getCKANResourceFlow: Flow[Marshallers.CkanData, CkanResource, NotUsed] =
    Flow[Marshallers.CkanData]
      .mapConcat(result => result.result.resources)
      .filter(resource =>
        resource.state == "active" &&
          supportedFormats.contains(resource.format) &&
          resource.url != null &&
          !resource.name.endsWith("readme.xls")
      )

  val processRecordFlow
      : Flow[Map[String, String], ProducerRecord[String, String], NotUsed] =
    Flow[Map[String, String]]
      .filter(filter => filter.nonEmpty)
      .map(item => ProducerRecord("city_budget_1", item.toString))

  val processRecordFlowHeader
      : Flow[Seq[String], ProducerRecord[String, String], NotUsed] =
    Flow[Seq[String]]
      .map(item => ProducerRecord("city_budget_1", item.toString))

  val processRecordFlowCells
      : Flow[Seq[String], ProducerRecord[String, String], NotUsed] =
    Flow[Seq[String]]
      .map(item => ProducerRecord("city_budget_1", item.toString))

  val xlsFlowTorontoHeader: Flow[InputStream, Seq[String], NotUsed] =
    Flow[InputStream].map(bs => {
      val workbook: Workbook = WorkbookFactory.create(bs)
      val worksheet: Sheet = workbook.getSheetAt(0)
      if (worksheet.getLastRowNum() > 0) {
        val headerRow = worksheet.getRow(0)
        val headerCells = headerRow.cellIterator()
        val formatter = new DataFormatter();
        val headers = headerCells.asScala
          .map(cell =>
            Option(formatter.formatCellValue(cell)) match {
              case Some("")    => "none"
              case Some(value) => value
              case None        => "none"
            }
          )
          .filter(item => item != "none")
        headers.toSeq
      } else {
        Seq("none")
      }
    })

  val xlsFlowTorontoCells: Flow[InputStream, Seq[String], NotUsed] =
    Flow[InputStream].mapConcat(bs => {
      val workbook: Workbook = WorkbookFactory.create(bs)
      val worksheet: Sheet = workbook.getSheetAt(0)
      if (worksheet.getLastRowNum() > 0) {
        val headerRow = worksheet.getRow(0)
        val headerCells = headerRow.cellIterator()
        val formatter = new DataFormatter();
        val dataRows = worksheet
          .rowIterator()
          .asScala
          .drop(1)
          .map(row => {
            val cells = row.cellIterator()
            val data = cells.asScala.map(cell => {
              Option(formatter.formatCellValue(cell)) match {
                case Some(value) => value
                case None        => "none"
              }
            })
            data.toSeq
          })
        dataRows.toSeq
      } else {
        Seq(Seq("none"))
      }

    })

  val xlsFlowToronto: Flow[InputStream, Map[String, String], NotUsed] =
    Flow[InputStream].mapConcat(bs => {
      val workbook: Workbook = WorkbookFactory.create(bs)
      val worksheet: Sheet = workbook.getSheetAt(0)
      if (worksheet.getLastRowNum() > 0) {
        val headerRow = worksheet.getRow(0)
        val headerCells = headerRow.cellIterator()
        val formatter = new DataFormatter();
        val headers = headerCells.asScala
          .map(cell =>
            Option(formatter.formatCellValue(cell)) match {
              case Some(value) => value
              case None        => "none"
            }
          )
          .toSeq
        val dataRows = worksheet
          .rowIterator()
          .asScala
          .drop(1)
          .map(row => {
            val cells = row.cellIterator()
            val data = cells.asScala.map(cell => {
              Option(formatter.formatCellValue(cell)) match {
                case Some("")    => "none"
                case Some(value) => value
                case None        => "none"
              }
            })
            data
          })
        val data = dataRows.map(row => {
          val data = row.zip(headers).toMap
          data
        })
        data
      } else {
        Seq(Map("none" -> "none"))
      }
    })

  // Sinks

  def convertHeader(header: String) = {
    header match {
      case "Program"                                  => "program"
      case "Service"                                  => "service"
      case "Activity"                                 => "activity"
      case "Expense/Revenue"                          => "expense_revenue"
      case "Category Name"                            => "category_name"
      case "Sub-Category Name"                        => "sub_category_name"
      case "Commitment item"                          => "commitment_item"
      case number if number.forall(Character.isDigit) => "amount"
      case _: String                                  => "none"
    }
  }

  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)
  val testSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  implicit val system: ActorSystem = ActorSystem()
  val execution: ExecutorService =
    java.util.concurrent.Executors.newSingleThreadExecutor()
  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(execution)

  

  def getResource(url: String) = {
    Http(system)
      .singleRequest(HttpRequest(uri = url))
      .flatMap(resp => resp.entity.dataBytes.runReduce(_ ++ _))
  }

  def collectDataIntoKafkaResource(url: String, sourceId: String) = {
    val http = Http(system)
    val resource: Source[CkanResource, NotUsed] = CkanDataCaseClasses().getCkanResources(url, sourceId)
      .via(getCKANResourceFlow)
    val is = resource
      .map(item => item.url)
      .log("Info", item => item.toString)
      .filter(url =>
        (url.endsWith(".xlsx") || url.endsWith(".xls")) && !url.endsWith(
          "readme.xls"
        )
      )
      .mapAsync(1)(simple => getResource(simple))
      .map(bs => new java.io.ByteArrayInputStream(bs.toArray))
      /** .map(is => { val worksheet = WorkbookFactory.create(is).getSheetAt(0)
        * if (worksheet.getLastRowNum() > 0) { val headerRow =
        * worksheet.getRow(0) val headerCells = headerRow.cellIterator() val
        * formatter = new DataFormatter(); val headers =
        * headerCells.asScala.map(cell =>
        * Option(formatter.formatCellValue(cell)) match { case Some(value) =>
        * value case None => "none" } ) val dataRows = worksheet .rowIterator()
        * .asScala .drop(1) .map(row => { val cells = row.cellIterator() val
        * data = cells.asScala.map(cell => {
        * Option(formatter.formatCellValue(cell)) match { case Some(value) =>
        * value case None => "none" } }) data }) val data = dataRows.map(row =>
        * { val data = row.zip(headers).toMap data }) data } else {
        * Seq(Map("none" -> "none")) } })
        */
      .via(xlsFlowToronto)
      .via(processRecordFlow)
      // .map(item => ProducerRecord("city_budget_1", item.mkString))
      .runWith(Producer.plainSink(producerSettings))
    is

  }

  

}
