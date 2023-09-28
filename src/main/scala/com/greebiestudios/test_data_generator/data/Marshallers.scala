package com.greebiestudios.test_data_generator.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import DefaultJsonProtocol._

object Marshallers extends SprayJsonSupport {

    abstract class CkanDataFormat {}
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
        resource_type: Option[String],
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
        resources: Seq[CkanResource],
    )
    final case class CkanData(
        help: String,
        success: Boolean,
        result: CkanResult,   
    ) extends CkanDataFormat

    final case class CkanProduct (
        sourceUrl: String,
        resourceId: String,
        payload: String) extends CkanDataFormat

    }

