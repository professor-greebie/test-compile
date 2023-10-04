package com.greebiestudios.test_data_generator.data

import spray.json.RootJsonFormat
import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import java.util.concurrent.ExecutorService
import scala.concurrent.ExecutionContextExecutorService
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.unmarshalling.Unmarshal

class CkanDataCaseClasses
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {

  implicit val system: ActorSystem = ActorSystem()
  val execution: ExecutorService =
    java.util.concurrent.Executors.newSingleThreadExecutor()
  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(execution)

  val ckanApiBoilerplate = "/api/3/action/package_show"
  val torontoBaseUrl = "https://ckan0.cf.opendata.inter.prod-toronto.ca"
  val packageList = Seq(
    "budget-operating-budget-program-summary-by-expenditure-category"
  )

  def getCkanResources(
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
    implicit val ckanDatasetFormat: RootJsonFormat[Marshallers.CkanDataset] =
      jsonFormat4(Marshallers.CkanDataset.apply)

  }

  abstract class CkanDataFormat
  final case class CkanOrganization(
      id: String,
      name: String,
      title: String,
      description: String,
      image_url: String,
      created: String,
      state: String
  )

  final case class CkanResource(
      package_id: String,
      id: String,
      size: Int,
      state: String,
      hash: String,
      format: String,
      last_modified: String,
      url_type: String,
      mimetype_inner: Option[String],
      mimetype: String,
      cache_url: Option[String],
      name: String,
      created: String,
      url: String,
      position: Int,
      resource_type: Option[String]
  )
  final case class CkanResult(
      license_title: String,
      maintainer: Option[String],
      maintainer_email: String,
      num_tags: Int,
      id: String,
      metadata_created: String,
      metadata_modified: String,
      organization: CkanOrganization,
      resources: Seq[CkanResource]
  )
  final case class CkanData(
      help: String,
      success: Boolean,
      result: CkanResult
  ) extends CkanDataFormat

  final case class CkanProduct(
      sourceUrl: String,
      resourceId: String,
      payload: String
  ) extends CkanDataFormat

  final case class CkanDataset(
      institution: String,
      sourceUrl: String,
      resourceId: String,
      payload: String
  ) extends CkanDataFormat

  final case class UftCkanProduct(
      service: String,
      activity: String,
      expenseRevenue: String,
      categoryName: String,
      subCategoryName: String,
      commitmentItem: String,
      amount: String
  )
}
