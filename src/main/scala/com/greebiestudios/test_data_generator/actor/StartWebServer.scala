package com.greebiestudios.test_data_generator.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import com.greebiestudios.test_data_generator.data.DataSource
import com.greebiestudios.test_data_generator.web.Routing

object StartWebServer {

  sealed trait ServerRunning
  final case class StartServer(name: String, port: Int) extends ServerRunning
  final case class StopServer(reason: String) extends ServerRunning

  def apply(): Behavior[ServerRunning] = {
    Behaviors.setup { context =>
      Behaviors.receiveMessage { message => message match 
        case StartServer(name, port) =>
            context.log.info("StartServer received")
            Routing(name, port)
            Behaviors.same
        case StopServer(reason) =>
            context.log.info("StopServer received for $reason")
            Behaviors.stopped
      }

    }
  }
}
