package com.greebiestudios.test_data_generator.data

import scala.util.Random
import scala.math.sqrt
import scala.math.log
import org.apache.commons.math3.distribution.ChiSquaredDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.RandomGeneratorFactory

trait Generator[T]:
  def generate(): T

enum GeneratorType:
  case Gaussian, RandomDouble, TDist, ChiSquared
end GeneratorType

class GeneratorImpl(
    val mean: scala.Double,
    val std: scala.Double,
    val degreesOfFreedom: scala.Int = 10,
    val gen: GeneratorType = GeneratorType.RandomDouble
) extends Generator[Double]:
  def result = gen match
    case GeneratorType.Gaussian     => createGaussianWithMeanAndStd(mean, std)
    case GeneratorType.RandomDouble => createDouble()
    case GeneratorType.TDist =>
      createTDistributionWithMeanAndStd(mean, std, degreesOfFreedom)
    case GeneratorType.ChiSquared =>
      createChiSquaredWithDegreesOfFreedom(degreesOfFreedom)

  def this(gen: GeneratorType) =
    this(0.0, 0.0)

  val rand: java.util.Random = java.util.Random()
  val random:  RandomGenerator = RandomGeneratorFactory.createRandomGenerator(rand)

  def createDouble(): Double = random.nextDouble()

  def createTDistributionWithMeanAndStd(
      mean: Double,
      std: Double,
      degreesOfFreedom: Int
  ): Double =
    random.nextGaussian()

  def createChiSquaredWithDegreesOfFreedom(degreesOfFreedom: Int): Double =
   ChiSquaredDistribution(random, degreesOfFreedom.toDouble).sample()

  def createGaussian(): Double =
    // Box-Muller transform via Marsaglia
    random.nextGaussian()

  def createGaussianWithMeanAndStd(mean: Double, std: Double): Double =
    mean + createGaussian() * std

  override def generate(): Double =
    result

end GeneratorImpl

class GeneratorLong(
    val generator: GeneratorImpl
) extends Generator[Long] {

  def createInt(): Int = generator.random.nextInt()

  override def generate(): Long = {
    val doubleValue = generator.generate()
    Math.round(doubleValue)
  }

}
