package com.greebiestudios.test_data_generator.data

import com.greebiestudios.test_data_generator.data.DataInput
import com.greebiestudios.test_data_generator.client.HttpService
import spray.json.RootJsonFormat
import com.greebiestudios.test_data_generator.util.UNZIP
import com.greebiestudios.test_data_generator.client.CSVService
import akka.stream.scaladsl.Sink
import java.util.stream.Collectors
import com.greebiestudios.test_data_generator.client.KafkaService
import akka.stream.scaladsl.Source
import org.checkerframework.checker.units.qual.s
import java.nio.charset.StandardCharsets

class FederalGovernmentData(using di: DataInput)
    extends HttpService(using di)
    with akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {

  def getDataSource(): DataInput = di

  object federalDataFormats {
    val unZipFLOW = new UNZIP().unZipFLOW
    implicit val averageRentFormat: RootJsonFormat[AverageRent] = jsonFormat13(
      AverageRent.apply
    )
    implicit val vacancyRateFormat: RootJsonFormat[VacancyRate] = jsonFormat11(
      VacancyRate.apply
    )
  }

  import federalDataFormats._
  import KafkaService._
  val federalDataSource = saveToFile()
        .via(unZipFLOW)
        .via(CSVService.csvFlow)
        .via(CSVService.csvToMap)
        .via(processKafkaRecordFlow("federalData22"))
        .runWith(kafkaSinkPlain)

  val demoSource = Source(List.from(1 to 2))
    .map(i => Source(Seq("a", "b", "c")))
    .map (src => src
      .map(item => Map("item" -> item))
      .via(processKafkaRecordFlow("federalData")))
    .runWith(Sink.foreach(item => kafkaSink(item)))



  sealed trait FederalData

  case class VacancyRate(
      refDate: String,
      geo: String,
      dguid: String,
      unit: String = "rate",
      vector: String,
      coordinate: String,
      value: Option[String],
      status: Option[String],
      symbol: Option[String],
      terminated: Option[String],
      decimals: Option[String]
  ) extends FederalData
  case class AverageRent(
      refDate: String,
      geo: String,
      dguid: String,
      structureType: String,
      unitType: String,
      unit: String = "dollars",
      vector: String,
      coordinate: String,
      value: Option[String],
      status: Option[String],
      symbol: Option[String],
      terminated: Option[String],
      decimals: Option[String]
  ) extends FederalData

}
