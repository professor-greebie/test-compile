package com.greebiestudios.test_data_generator.data

import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringSerializer



class Producers {
    val configFile = ConfigFactory.load("application.conf")
    val config = configFile.getConfig("akka.kafka.producer")
    val bootstrapServers = configFile.getString("akka.kafka.producer.bootstrap-servers")

    val producerSettings =
       ProducerSettings(config, new StringSerializer, new StringSerializer).withBootstrapServers(bootstrapServers)
    val producer = Producer.plainSink(producerSettings)

    //val oceanBuoy = Source(1 to 100).map(_.toString).runWith(Producer.plainSink(producerSettings))
    //val kafkaProducer: Future[org.apache.kafka.clients.producer.Producer[String, String]] = producerSettings.createKafkaProducer()

}


