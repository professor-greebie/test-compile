package com.greebiestudios.test_data_generator.web

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.get
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import scala.io.StdIn
import akka.stream.scaladsl.Source
import scala.compiletime.ops.double
import akka.http.scaladsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import com.greebiestudios.test_data_generator.data.NumberValue
import akka.NotUsed
import com.greebiestudios.test_data_generator.data.Generator
import com.greebiestudios.test_data_generator.data.GeneratorImpl
import com.greebiestudios.test_data_generator.data.GeneratorType
import spray.json.RootJsonFormat
import com.greebiestudios.test_data_generator.data.GeneratorLong
import akka.actor.Cancellable
import akka.util.ByteString
import akka.stream.scaladsl.Flow
import scala.concurrent.Future
import akka.actor.typed.ActorRef
import com.greebiestudios.test_data_generator.data.NumberValue
import com.greebiestudios.test_data_generator.data.Identity

object Routing
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
    
  val newLine = ByteString("\n")
  
  val valueStreams = ValueStreams
  val nameStreams = NameStreams

  def apply(): Unit = {
    implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json().withFramingRenderer(Flow[ByteString].map(byteString => byteString ++ newLine))

    implicit val numberValueFormat: RootJsonFormat[NumberValue] = jsonFormat1(
      NumberValue.apply
    )
    implicit val nameValueFormat: RootJsonFormat[Identity] = jsonFormat2(Identity.apply)

    implicit val system: ActorSystem[Any] =
      ActorSystem(Behaviors.empty, "test-data-system")
    implicit val executionContext =
      system.executionContext


    lazy val route: Route = concat (
      path("api" / "gaussian" / "whole" / LongNumber / LongNumber){ (mean, std) =>
        parameter("seed".optional) { seed => 
          get 
            complete(
              valueStreams.getGaussianInt(mean, std, seed.map(i => i.toInt))
            )
        }},
      path("api" / "gaussian" / "real" / LongNumber / LongNumber) { (mean, std) =>
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getGaussian(mean, std, seed.map(i => i.toInt))
            )
        }},
      path("api" / "chi-squared" / "real" / IntNumber) { (dof) => 
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getChiSquared(dof, seed.map(i => i.toInt))
            )
        }},
      path("api" / "chi-squared" / "whole" / IntNumber) { (dof) => 
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getChiSquaredLong(dof, seed.map(i => i.toInt))
            )
        }},
      path("api" / "beta" / "real" / LongNumber / LongNumber) { (a, b) =>
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getBeta(a, b, seed.map(i => i.toInt))
            )
          }},
      path("api" / "beta" / "whole" / LongNumber / LongNumber) { (a, b) =>
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getBetaLong(a, b, seed.map(i => i.toInt))
            )
          }},
      path("api" / "exponential" / "real" / LongNumber) { (mean) =>
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getExponential(mean, seed.map(i => i.toInt))
            )
        }},
        path("api" / "exponential" / "whole" / LongNumber) { (mean) =>
        parameter("seed".optional) { seed => 
          get
            complete(
              valueStreams.getExponentialLong(mean, seed.map(i => i.toInt))
            )
        }},
        path("api" / "names") {
          get 
            complete(
              nameStreams.getNames(None)
            )
          }
      )

    val bindingFuture = Http().newServerAt("0.0.0.0", 8099).bind(route)
    println(
      s"Server now online. Please navigate to http://localhost:8099/hello\nPress RETURN to stop..."
    )
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
