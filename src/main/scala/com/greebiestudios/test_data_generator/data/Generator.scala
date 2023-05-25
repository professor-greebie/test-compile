package com.greebiestudios.test_data_generator.data

import scala.util.Random
import scala.math.sqrt
import scala.math.log
import org.apache.commons.math3.distribution.ChiSquaredDistribution
import org.apache.commons.math3.distribution.BetaDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.RandomGeneratorFactory
import spray.json.JsNumber
import org.apache.commons.math3.distribution.ExponentialDistribution
import spray.json.JsValue
import spray.json.JsString

trait Generator[T]:
  var seed: Option[Int]
  def generate(): T

enum GeneratorType:
  case Gaussian, RandomDouble, TDist, ChiSquared, Beta, Exponential
end GeneratorType

class NameGenerator(val wordList: Seq[String]) extends Generator[String]:
  private var _seed: scala.Option[Int] = None

  def seed: Option[Int] = _seed
  def seed_= (nval: scala.Option[Int]): Unit = _seed = nval

  def rand: java.util.Random = _seed match {
    case Some(seedVal) => java.util.Random(seedVal)
    case None => java.util.Random()
  }
  lazy val random = rand


  def generate(): String = {
    wordList(random.nextInt(wordList.length))
  }

end NameGenerator



class GeneratorImpl(
    val sampleValueA: scala.Double, // usually a mean
    val sampleValueB: scala.Double, // usually, but not always a standard deviation
    val degreesOfFreedom: scala.Int = 10,
    val gen: GeneratorType = GeneratorType.RandomDouble
) extends Generator[Double]:
  
  private var _seed: scala.Option[Int] = None

  def seed: Option[Int] = _seed
  def seed_= (nval: scala.Option[Int]): Unit = _seed = nval

  def result = gen match
    case GeneratorType.Gaussian     => createGaussianWithMeanAndStd(sampleValueA, sampleValueB)
    case GeneratorType.RandomDouble => createDouble()
    case GeneratorType.TDist =>
      createTDistributionWithMeanAndStd(sampleValueA, sampleValueB, degreesOfFreedom)
    case GeneratorType.ChiSquared =>
      createChiSquaredWithDegreesOfFreedom(degreesOfFreedom)
    case GeneratorType.Beta => 
      createBetaSample(sampleValueA, sampleValueB)
    case GeneratorType.Exponential => 
      createExponentialSample(sampleValueA)

  def this(gen: GeneratorType) =
    this(0.0, 0.0)

  def rand: java.util.Random = _seed match {
    case Some(seedVal) => java.util.Random(seedVal)
    case None => java.util.Random()
  }
  lazy val random:  RandomGenerator = RandomGeneratorFactory.createRandomGenerator(rand)

  def createDouble(): Double = random.nextDouble()

  def createTDistributionWithMeanAndStd(
      sampleValueA: Double,
      sampleValueB: Double,
      degreesOfFreedom: Int
  ): Double =
    random.nextGaussian()

  def createChiSquaredWithDegreesOfFreedom(degreesOfFreedom: Int): Double =
   ChiSquaredDistribution(random, degreesOfFreedom.toDouble).sample()

  def createGaussian(): Double =
    // Box-Muller transform via Marsaglia
    random.nextGaussian()

  def createGaussianWithMeanAndStd(sampleValueA: Double, sampleValueB: Double): Double =
    sampleValueA + createGaussian() * sampleValueB

  def createBetaSample(sampleValueA: Double, sampleValueB: Double): Double = 
    BetaDistribution(random, sampleValueA, sampleValueB).sample()

  def createExponentialSample(sampleValueA: Double): Double = 
    ExponentialDistribution(random, sampleValueA).sample()

  def getResult(): Double = result
  override def generate(): Double =
    result

end GeneratorImpl

class GeneratorLong(
    val generator: GeneratorImpl
) extends Generator[Long] {

  def seed: Option[Int] = generator.seed
  def seed_= (nval: scala.Option[Int]): Unit = generator.seed = nval

  def createInt(): Int = generator.random.nextInt()

  override def generate(): Long = {
    val doubleValue: Double = generator.getResult()
    Math.round(doubleValue)
  }

}
