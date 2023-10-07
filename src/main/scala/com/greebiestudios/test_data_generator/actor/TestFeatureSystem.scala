package com.greebiestudios.test_data_generator.actor

import com.greebiestudios.test_data_generator.actor.GetFinancialData.apply
import akka.actor.typed.scaladsl.Behaviors
import com.greebiestudios.test_data_generator.data.OCRDataSource
import akka.actor.typed.Behavior
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext

object TestFeatureSystem {

  trait TestFeatureSystemRunning

  final case class StartTestSystemOCRTest() extends TestFeatureSystemRunning
  final case class StartTestSystemActorTest() extends TestFeatureSystemRunning

  def apply(): Behavior[TestFeatureSystemRunning] = {
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case StartTestSystemOCRTest() => 
          context.log.info("StartTestSystemOCRTest received")
          val ocr = new OCRDataSource()
          ocr.printText()
          Behaviors.empty
        case StartTestSystemActorTest() =>
          context.log.info("StartTestSystemActorTest received")
          Behaviors.same
    }}}
}
