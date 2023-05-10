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

object Routing
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
    
  val newLine = ByteString("\n")
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json().withFramingRenderer(Flow[ByteString].map(byteString => byteString ++ newLine))

  

  def getGaussian(
      mean: Double,
      std: Double
  ): Source[NumberValue, Cancellable] = {
    val generator: Generator[Double] =
      GeneratorImpl(mean, std, 10, gen = GeneratorType.Gaussian)
    val gaussian: Source[NumberValue, Cancellable] = Source
      .tick[NumberValue](
        250.millis,
        500.millis,
        NumberValue(mean))
      .map(_ => NumberValue(generator.generate())) 
      .named("Gaussian")
    gaussian
  }

  def getGaussianInt(
      mean: Long,
      std: Long
  ): Source[NumberValue, Cancellable] = {
    val generatorLong: Generator[Long] = GeneratorLong(
      GeneratorImpl(
        mean.toDouble,
        std.toDouble,
        10,
        gen = GeneratorType.Gaussian
      )
    )
    val gaussian: Source[NumberValue, Cancellable] = Source
      .tick[NumberValue](
        250.millis,
        500.millis,
        NumberValue(generatorLong.generate())
      ).map(_ => NumberValue(generatorLong.generate()))
      .named("Gaussian Long")
    gaussian
  }

  def apply(): Unit = {
    implicit val numberValueFormat: RootJsonFormat[NumberValue] = jsonFormat1(
      NumberValue.apply
    )
    implicit val system: ActorSystem[Any] =
      ActorSystem(Behaviors.empty, "test-data-system")
    implicit val executionContext =
      system.executionContext


    lazy val route: Route = concat (
      path("api" / "gaussian" / "whole" / LongNumber / LongNumber){ (mean, std) =>
          get 
            complete(
              getGaussianInt(mean, std)
            )
        },
      path("api" / "gaussian" / "real" / LongNumber / LongNumber) { (mean, std) =>
          get
            complete(
              getGaussian(mean, std)
            )
      }
      )

    val bindingFuture = Http().newServerAt("localhost", 8099).bind(route)
    println(
      s"Server now online. Please navigate to http://localhost:8099/hello\nPress RETURN to stop..."
    )
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
