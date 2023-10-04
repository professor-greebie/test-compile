package com.greebiestudios.test_data_generator.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import com.greebiestudios.test_data_generator.web.Routing
import akka.NotUsed
import com.greebiestudios.test_data_generator.data.Marshallers.CkanDataFormat
import com.greebiestudios.test_data_generator.data.DataSource

object RunSystem {
    trait SystemRunning
    final case class StartSystem() extends SystemRunning
    final case class StopSystem(reason: String) extends SystemRunning
    final case class GetData() extends SystemRunning

    def apply(): Behavior[SystemRunning] = Behaviors.setup{ context =>
        context.log.info("RootActorSystem started")
        val routing = context.spawn(StartWebServer(), "WebServer")
        val getFinancialData = context.spawn(GetFinancialData(), "GetFinancialData")
        Behaviors.receiveMessage{ 
            case StartSystem() => 
                context.log.info("StartSystem received")
                routing ! StartWebServer.StartServer("Math API", 8099)
                Behaviors.same
            case StopSystem(reason) => 
                context.log.info("StopSystem received")
                routing ! StartWebServer.StopServer(reason)
                Behaviors.stopped
            case GetData() => 
                context.log.info("GetData received")
                getFinancialData ! GetFinancialData.GetFinancialData(DataSource.CKAN, "url")
                Behaviors.same
            case _ => 
                context.log.info("Unknown message received")
                Behaviors.same
        } 
    }}

