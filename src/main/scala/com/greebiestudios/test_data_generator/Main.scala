package com.greebiestudios.test_data_generator

import com.greebiestudios.test_data_generator.web.Routing
import com.greebiestudios.test_data_generator.data.ApiSourceInformation
import akka.protobufv3.internal.Api
import com.greebiestudios.test_data_generator.actor.RootActorSystem
import com.greebiestudios.test_data_generator.actor.StartWebServer
import akka.actor.typed.ActorSystem
import akka.NotUsed
import com.greebiestudios.test_data_generator.actor.RunSystem
import akka.actor.typed.ActorRef


object App {
  @main 
  def launch: Unit =
    implicit val system: ActorSystem[RunSystem.SystemRunning] = RootActorSystem()
    implicit val context = system.executionContext
  }