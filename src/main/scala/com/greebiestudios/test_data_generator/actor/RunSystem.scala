package com.greebiestudios.test_data_generator.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import com.greebiestudios.test_data_generator.web.Routing
import akka.NotUsed
import com.greebiestudios.test_data_generator.data.Marshallers.CkanDataFormat
import com.greebiestudios.test_data_generator.data.DataSource
import concurrent.duration.DurationInt
import akka.actor.typed.ActorRef


object RunSystem {
    trait SystemRunning
    final case class StartSystem() extends SystemRunning
    final case class StopSystem(reason: String) extends SystemRunning
    final case class GetData() extends SystemRunning
    final case class GetTestFeature() extends SystemRunning

    def apply(): Behavior[SystemRunning] = Behaviors.setup{ context =>
        context.log.info("RootActorSystem started")
        val routing = context.spawn(StartWebServer(), "WebServer")
        val getFinancialData = context.spawn(GetFinancialData(), "GetFinancialData")
        val testFeatures: ActorRef[TestFeatureSystem.TestFeatureSystemRunning] = context.spawn(TestFeatureSystem(), "TestFeatureSystem")
        
          Behaviors.receiveMessage{ 
            case StartSystem() => 
                context.log.info("StartSystem received")
                /**
                Behaviors.withTimers{ timers =>
                  timers.startTimerWithFixedDelay(GetData(), 1.second, 10.minutes)
                  Behaviors.same
                } **/
                Behaviors.withTimers{ timers =>
                  timers.startTimerWithFixedDelay(GetData(), 1.second, 10.minutes)
                  Behaviors.same
                }
                //routing ! StartWebServer.StartServer("Math API", 8099)
            case StopSystem(reason) => 
                context.log.info("StopSystem received")
                routing ! StartWebServer.StopServer(reason)
                Behaviors.stopped
            case GetData() => 
                context.log.info("GetData received")
                getFinancialData ! GetFinancialData.GetHousingData(DataSource.CSV, "")
                Behaviors.same
            case GetTestFeature() => 
                context.log.info("GetTestFeature received")            
                testFeatures ! TestFeatureSystem.StartTestSystemOCRTest()
                Behaviors.same
            case _ => 
                context.log.info("Unknown message received")
                Behaviors.same
        }
    }
}

