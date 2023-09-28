package com.greebiestudios.test_data_generator.data

import akka.actor._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.Http
import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.Future
import spray.json.RootJsonFormat
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

class ApiSourceInformation
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
  val configFile = ConfigFactory.load("application.conf")
  val config = configFile.getConfig("akka.kafka.producer")
  val bootstrapServers =
    configFile.getString("akka.kafka.producer.bootstrap-servers")

  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  implicit val system: ActorSystem = ActorSystem()
  val execution: ExecutorService =
    java.util.concurrent.Executors.newSingleThreadExecutor()
  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(execution)

  object ckanJsonFormats {
    implicit val ckanOrganizationFormat
        : RootJsonFormat[Marshallers.CkanOrganization] = jsonFormat7(
      Marshallers.CkanOrganization.apply
    )
    implicit val ckanResourceFormat: RootJsonFormat[Marshallers.CkanResource] =
      jsonFormat16(
        Marshallers.CkanResource.apply
      )
    implicit val ckanResultFormat: RootJsonFormat[Marshallers.CkanResult] =
      jsonFormat9(
        Marshallers.CkanResult.apply
      )
    implicit val ckanDataFormat: RootJsonFormat[Marshallers.CkanData] =
      jsonFormat3(
        Marshallers.CkanData.apply
      )
    implicit val ckanProductFormat: RootJsonFormat[Marshallers.CkanProduct] =
      jsonFormat3(Marshallers.CkanProduct.apply)

  }

  val ckanApiBoilerplate = "/api/3/action/package_show"
  val torontoBaseUrl = "https://ckan0.cf.opendata.inter.prod-toronto.ca"
  val packageList = Seq(
    "budget-operating-budget-program-summary-by-expenditure-category"
  )

  def getResources(
      url: String,
      sourceId: String
  ): Source[Marshallers.CkanData, NotUsed] = {
    import ckanJsonFormats._
    val params = Map("id" -> sourceId)
    val uri: Uri = Uri(url + ckanApiBoilerplate).withQuery(Uri.Query(params))
    val http = Http(system)
    val request = HttpRequest(uri = uri)
    val response = http
      .singleRequest(request)
      .flatMap(resp => Unmarshal(resp.entity).to[Marshallers.CkanData])
    Source.future[Marshallers.CkanData](response)
  }

  def collectDataIntoKafkaResource(url: String, sourceId: String) = {
    import ckanJsonFormats._
    val http = Http(system)
    val resource = getResources(url, sourceId)
      .flatMapConcat(result => Source(result.result.resources))
      .map(resource => http.singleRequest(HttpRequest(uri = resource.url)))
      .map(response =>
        response.flatMap(resp => Unmarshal(resp.entity).to[String])
      )
      .map(response =>
        response.map(resp => Marshallers.CkanProduct(url, sourceId, resp))
      ).map(value => new ProducerRecord[String, String]("city_budget", value.toString))
      .runWith(Producer.plainSink(producerSettings))
  }

}
