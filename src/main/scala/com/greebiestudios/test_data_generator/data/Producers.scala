package com.greebiestudios.test_data_generator.data

import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.Source

class Producers {
    val config = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
       ProducerSettings(config, new StringSerializer, new StringSerializer).withBootstrapServers(bootstrapServers)

    val oceanBuoy = Source(1 to 100).map(_.toString).runWith(Producer.plainSink(producerSettings))
    val kafkaProducer: Future[org.apache.kafka.clients.producer.Producer[String, String]] = producerSettings.createKafkaProducer()

}


