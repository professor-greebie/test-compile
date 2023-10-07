package com.greebiestudios.test_data_generator.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.NotUsed

object RootActorSystem {
    val system = ActorSystem(RunSystem(), "Root_Actor_System") 

    def apply(): ActorSystem[RunSystem.SystemRunning] = {
        startServer()
        system
    }

    def startServer(): Unit = {
        system ! RunSystem.StartSystem()
        //system ! RunSystem.GetTestFeature()
        
    }

    /// TODO: Add logic to stop server without stopping system
    /// TODO: Add logic to time collecting data from various endpoints
    
}
