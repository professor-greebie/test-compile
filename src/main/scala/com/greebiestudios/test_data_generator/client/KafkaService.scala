package com.greebiestudios.test_data_generator.client

import com.typesafe.config.ConfigFactory
import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.ProducerRecord
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.stream.scaladsl.Sink
import akka.Done
import scala.concurrent.Future
import com.greebiestudios.test_data_generator.actor.RunSystem
import com.greebiestudios.test_data_generator.actor.RootActorSystem
import scala.concurrent.ExecutionContext
import akka.stream.scaladsl.Source

object KafkaService {
  implicit val system: akka.actor.typed.ActorSystem[RunSystem.SystemRunning] =
    RootActorSystem()
  implicit val ec: ExecutionContext = system.executionContext


  val configFile = ConfigFactory.load("application.conf")
  val config = configFile.getConfig("akka.kafka.producer")
  val bootstrapServers =
    configFile.getString("akka.kafka.producer.kafka-clients.bootstrap.servers")
  val producerSettings =
    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  val kafkaProducer = producerSettings.createKafkaProducerAsync()
  val kafkaSinkPlain = Producer.plainSink(producerSettings)

  def kafkaSink(src: Source[ProducerRecord[String, String], Any]) = kafkaProducer.foreach(producer => {
    src.runWith(Producer.plainSink(producerSettings))
    producer.close()
  })
  def apply()= {}

  def processKafkaRecordFlow(topic: String)
      : Flow[Map[String, String], ProducerRecord[String, String], NotUsed] =
    Flow[Map[String, String]]
      .filter(filter => filter.nonEmpty)
      .map(item => ProducerRecord(topic, item.toString))

}