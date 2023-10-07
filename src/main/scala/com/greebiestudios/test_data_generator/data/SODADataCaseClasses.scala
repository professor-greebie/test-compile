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
import scala.concurrent.Future
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model._

import akka.stream.scaladsl.Merge

class SODADataCaseClasses
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport {
  implicit val system: akka.actor.typed.ActorSystem[RunSystem.SystemRunning] =
    RootActorSystem()
  implicit val ec: ExecutionContext = system.executionContext

  object Run {
    def apply(): Unit = {}
  }
    implicit val edmontonBudgetDataFormat: RootJsonFormat[EdmontonBudgetData] =
      jsonFormat7(EdmontonBudgetData.apply)
    implicit val winnipegBudgetDataFormat: RootJsonFormat[WinnipegBudgetData] =
        jsonFormat13(WinnipegBudgetData.apply)
    implicit val winnipegExpendituresDataFormat: RootJsonFormat[WinnipegExpendituresData] =
        jsonFormat14(WinnipegExpendituresData.apply)


  val EdmontonBaseUrl = "https://data.edmonton.ca/resource/"
  val EdmontonBudgetDataUrl = "qycq-4ckj.json"
  val WinnipegBaseUrl = "https://data.winnipeg.ca/resource/"
  val WinnipegBudgetDataUrl = "hk5w-mj8b.json"
  val WinnipegExpendituresDataUrl = "8xrn-n992.json"

  
  val edmontonFlow:Flow[ByteString, BudgetData, NotUsed] = 
    Flow[ByteString]
      .mapAsync(1)(bs => Unmarshal(bs).to[EdmontonBudgetData])
      .map(edmontonData => BudgetData(
        "Edmonton",
        edmontonData.year,
        edmontonData.department_agencies,
        edmontonData.branch,
        edmontonData.status,
        edmontonData.budget_category,
        edmontonData.budget_sub_category,
        "",
        "",
        "",
        "",
        edmontonData.dollar_amount,
      ))
  val winnipegFlow: Flow[ByteString, BudgetData, NotUsed] = 
    Flow[ByteString]
      .mapAsync(1)(x => Unmarshal(x).to[WinnipegExpendituresData])
      .map(winnipegData => BudgetData(
        "Winnipeg",
        Option(winnipegData.project_year).getOrElse(""),
        Option(winnipegData.department).getOrElse(""),
        Option(winnipegData.tax_supported_utilities).getOrElse(""),
        Option(winnipegData.project_description).getOrElse(""),
        Option(winnipegData.category).getOrElse(""),
        winnipegData.subcategory.getOrElse(""),
        Option(winnipegData.project_id).getOrElse(""),
        Option(winnipegData.adopted_budget).getOrElse(""),
        Option(winnipegData.amended_budget).getOrElse(""),
        Option(winnipegData.available_balance).getOrElse(""),
        Option(winnipegData.spent).getOrElse("")))

  // Flows
  def getSodaResource(
      url: String,
      resource: String, 
      flow: Flow[ByteString, BudgetData, NotUsed]
  ): Source[BudgetData, NotUsed] = {
    val http = Http()
    val uri: Uri = Uri(url + resource)
    val request = HttpRequest(uri = uri)
    val response = http
      .singleRequest(request)
      .flatMap(resp =>
        resp.entity.dataBytes
          .via(JsonFraming.objectScanner(Int.MaxValue))
          .via(flow)
          .runWith(Sink.seq)
      )
    Source.future(response).mapConcat(identity)
  }

  def getEdmontonBudgetData() = {
    val apiSourceInformation = new ApiSourceInformation()
    val edmontonBudgetData =
      getSodaResource(EdmontonBaseUrl, EdmontonBudgetDataUrl, edmontonFlow)
        .map(x => x.productElementNames.zip(x.productIterator.map((item) => item.toString())).toMap)
        .via(apiSourceInformation.processRecordFlow("city_budget_1"))
        .runWith(apiSourceInformation.kafkaSink)
    edmontonBudgetData
  }

  def getWinnipegBudgetData() = {
    val apiSourceInformation = new ApiSourceInformation()
    val winnipegBudgetData = 
        getSodaResource(WinnipegBaseUrl, WinnipegExpendituresDataUrl, winnipegFlow)
          .map(x => x.productElementNames.zip(x.productIterator.map((item) => item.toString())).toMap)
          .via(apiSourceInformation.processRecordFlow("city_budget_1"))
          .log("Info", item => "Got item: ${item.toString}")
          .runWith(apiSourceInformation.kafkaSink)
    winnipegBudgetData
  }

  def getAllBudgetData() = {
    val apiSourceInformation = new ApiSourceInformation()
    val edmontonBudgetData =
      getSodaResource(EdmontonBaseUrl, EdmontonBudgetDataUrl, edmontonFlow)
        .map(x => x.productElementNames.zip(x.productIterator.map((item) => item.toString())).toMap)
        .via(apiSourceInformation.processRecordFlow("city_budget_1"))
    val winnipegBudgetData = 
        getSodaResource(WinnipegBaseUrl, WinnipegExpendituresDataUrl, winnipegFlow)
          .map(x => x.productElementNames.zip(x.productIterator.map((item) => item.toString())).toMap)
          .via(apiSourceInformation.processRecordFlow("city_budget_1"))
    Source.combine(edmontonBudgetData, winnipegBudgetData)(Merge(_))
      .runWith(apiSourceInformation.kafkaSink)

  }

  

  sealed trait SODAData

  case class EdmontonBudgetData(
      year: String,
      department_agencies: String,
      branch: String,
      status: String,
      budget_category: String,
      budget_sub_category: String,
      dollar_amount: String
  ) extends SODAData

  case class WinnipegBudgetData(
    project_name: String,
    department: String,
    project_number: String,
    our_winnipeg: String,
    service: String, 
    previous_budgets_note: String,
    previous_budgets: String, 
    _2014_adopted: String,
    _2015_forecast: String,
    _2016_forecast: String,
    _2017_forecast: String,
    _2018_forecast: String,
    _2019_forecast: String,
  ) extends SODAData

  case class WinnipegExpendituresData(
    date: String,
    tax_supported_utilities: String,
    department: String,
    category: String,
    subcategory: Option[String],
    project_id: String,
    project_description: String,
    project_year: String,
    adopted_budget: String,
    amended_budget: String,
    actual_costs_to_report_date: String,
    available_balance: String,
    spent: String,
    date_created: String,
  ) extends SODAData

}
