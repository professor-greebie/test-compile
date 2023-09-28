package com.greebiestudios.test_data_generator

import com.greebiestudios.test_data_generator.web.Routing
import com.greebiestudios.test_data_generator.data.ApiSourceInformation
import akka.protobufv3.internal.Api


object App {
  @main def launch: Unit =
    Routing()
    ApiSourceInformation().collectDataIntoKafkaResource(ApiSourceInformation().torontoBaseUrl, ApiSourceInformation().packageList(0))
  }