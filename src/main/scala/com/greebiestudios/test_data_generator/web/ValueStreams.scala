package com.greebiestudios.test_data_generator.web

import com.greebiestudios.test_data_generator.data.NumberValue
import akka.actor.Cancellable
import akka.stream.scaladsl.Source
import scala.concurrent.duration._
import com.greebiestudios.test_data_generator.data.Generator
import com.greebiestudios.test_data_generator.data.GeneratorType
import com.greebiestudios.test_data_generator.data.GeneratorImpl
import com.greebiestudios.test_data_generator.data.GeneratorLong
import spray.json.JsValue
import spray.json.JsNumber

object ValueStreams {
  def tickStream(fun: () => JsNumber): Source[NumberValue, Cancellable] = {
    val source: Source[NumberValue, Cancellable] = Source
      .tick[NumberValue](250.millis, 500.millis, NumberValue(JsNumber(0.0)))
      .map(_ => NumberValue(fun()))
    source
  }

  def getGaussian(
      mean: Double,
      std: Double,
      seed: scala.Option[Int] = None
  ): Source[NumberValue, Cancellable] = {
    val generator: Generator[Double] =
      GeneratorImpl(mean, std, 10, gen = GeneratorType.Gaussian)
    generator.seed = seed
    val gaussian: Source[NumberValue, Cancellable] =
      tickStream(generator.generate)
        .named("Gaussian")
    gaussian
  }

  def getGaussianInt(
      mean: Long,
      std: Long,
      seed: Option[Int] = None
  ): Source[NumberValue, Cancellable] = {
    val generatorLong: Generator[Long] = GeneratorLong(
      GeneratorImpl(
        mean.toDouble,
        std.toDouble,
        10,
        gen = GeneratorType.Gaussian
      )
    )
    generatorLong.seed = seed
    val gaussian: Source[NumberValue, Cancellable] =
      tickStream(generatorLong.generate)
        .named("Gaussian Long")
    gaussian
  }
}
