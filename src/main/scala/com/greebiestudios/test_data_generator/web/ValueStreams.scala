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
import spray.json.JsString

object ValueStreams extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
  def tickStream(fun: () => JsValue): Source[NumberValue, Cancellable] = {
    val source: Source[NumberValue, Cancellable] = Source
      .tick[NumberValue](250.millis, 500.millis, NumberValue(JsNumber(0.0)))
      .map(_ => NumberValue(JsString(fun().convertTo[String])))
    source
  }

  def getMunicipalExpenditures() : Source[NumberValue, Cancellable] = {
    val municipalExpenditures: Source[NumberValue, Cancellable] = 
      tickStream(() => JsString("Municipal Expenditures"))
        .named("Municipal Expenditures")
    municipalExpenditures
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
      tickStream(() => JsString(generator.generate().toString()))
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
      tickStream(() => JsString(generatorLong.generate().toString()))
        .named("Gaussian Long")
    gaussian
  }

  def getChiSquared(dof: Int, seed: Option[Int] = None
    ): Source[NumberValue, Cancellable] = {
      val generatorChi: Generator[Double] = GeneratorImpl(0, 0, dof, gen = GeneratorType.ChiSquared)
      generatorChi.seed = seed
      val chiSquared: Source[NumberValue, Cancellable] = 
        tickStream(() => JsString(generatorChi.generate().toString()))
          .named("ChiSquared")
      chiSquared
    }

  def getChiSquaredLong(dof: Int, seed: Option[Int] = None
    ): Source[NumberValue, Cancellable] = {
      val generatorChi: Generator[Long] = GeneratorLong(GeneratorImpl(0, 0, dof, gen = GeneratorType.ChiSquared))
      generatorChi.seed = seed
      val chiSquared: Source[NumberValue, Cancellable] = 
        tickStream(() => JsString(generatorChi.generate().toString()))
          .named("ChiSquared")
      chiSquared
    }
  def getBeta(valueA: Double, valueB: Double, seed:Option[Int] = None): Source[NumberValue, Cancellable] = {
    val generatorBeta: Generator[Double] = GeneratorImpl(valueA, valueB, 10, gen = GeneratorType.Beta)
    generatorBeta.seed = seed
    val beta: Source[NumberValue, Cancellable] = 
      tickStream(() => JsString(generatorBeta.generate().toString()))
        .named("Beta")
    beta
  }

  // Need to determine if this is necessary. Whole numbers for prob dist is 0.
  // Could return the prob as percentage though.
  def getBetaLong(valueA: Double, valueB: Double, seed: Option[Int] = None) : Source [NumberValue, Cancellable] = {
    val generatorBetaLong: Generator[Long] = GeneratorLong(GeneratorImpl(valueA, valueB, 10, gen = GeneratorType.Beta))
    generatorBetaLong.seed = seed
    val beta: Source[NumberValue, Cancellable] = 
      tickStream(() => JsString(generatorBetaLong.generate().toString()))
        .named("BetaLong")
    beta
  }

  def getExponential(valueA: Double, seed: Option[Int]= None) : Source [ NumberValue, Cancellable] = {
    val generatorExponential: Generator[Double] = GeneratorImpl(valueA, 0, 10, gen = GeneratorType.Exponential)
    generatorExponential.seed = seed
    val exponential: Source[NumberValue, Cancellable] = 
      tickStream(() => JsString(generatorExponential.generate().toString()))
        .named("Exponential")
    exponential
  }

  def getExponentialLong(valueA: Double, seed: Option[Int]= None) : Source [ NumberValue, Cancellable] = {
    val generatorExponential: Generator[Long] = GeneratorLong(GeneratorImpl(valueA, 0, 10, gen = GeneratorType.Exponential))
    generatorExponential.seed = seed
    val exponential: Source[NumberValue, Cancellable] = 
      tickStream(() => JsString(generatorExponential.generate().toString()))
        .named("Exponential")
    exponential
  }
}
