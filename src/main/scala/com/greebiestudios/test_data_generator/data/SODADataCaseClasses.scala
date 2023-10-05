package com.greebiestudios.test_data_generator.data

import akka.actor.typed.ActorSystem
import com.greebiestudios.test_data_generator.actor.RunSystem
import com.greebiestudios.test_data_generator.actor.RootActorSystem
import scala.concurrent.ExecutionContext
import akka.stream.scaladsl.JsonFraming
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpEntity
import akka.util.ByteString
import akka.http.scaladsl.unmarshalling.Unmarshal
import netscape.javascript.JSObject
import spray.json.RootJsonFormat
import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.model._
import akka.NotUsed
import akka.stream.scaladsl.Sink

class SODADataCaseClasses
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport {
  implicit val system: akka.actor.typed.ActorSystem[RunSystem.SystemRunning] =
    RootActorSystem()
  implicit val ec: ExecutionContext = system.executionContext

  object Run {
    def apply(): Unit = {}
  }

  val EdmontonBaseUrl = "https://data.edmonton.ca/resource/"
  val EdmontonBudgetDataUrl = "qycq-4ckj.json"

  // Flows
  def getSodaResource(
      url: String,
      resource: String
  ): Source[EdmontonBudgetData, NotUsed] = {
    import jsonFormats._
    val http = Http()
    val uri: Uri = Uri(url + resource)
    val request = HttpRequest(uri = uri)
    val response = http
      .singleRequest(request)
      .flatMap(resp =>
        resp.entity.dataBytes
          .via(JsonFraming.objectScanner(Int.MaxValue))
          .mapAsync(1)(json => Unmarshal(json).to[EdmontonBudgetData])
          .runWith(Sink.seq)
      )

    Source.future(response).mapConcat(identity)
  }

  def getEdmontonBudgetData() = {
    val apiSourceInformation = new ApiSourceInformation()
    val edmontonBudgetData =
      getSodaResource(EdmontonBaseUrl, EdmontonBudgetDataUrl)
        .map(x => x.productElementNames.zip(x.productIterator.map((item) => item.toString())).toMap)
        .via(apiSourceInformation.processRecordFlow)
        .runWith(apiSourceInformation.kafkaSink)
    edmontonBudgetData
  }

  object jsonFormats {
    implicit val edmontonBudgetDataFormat: RootJsonFormat[EdmontonBudgetData] =
      jsonFormat7(EdmontonBudgetData.apply)

  }

  case class EdmontonBudgetData(
      year: String,
      department_agencies: String,
      branch: String,
      status: String,
      budget_category: String,
      budget_sub_category: String,
      dollar_amount: String
  )

}
