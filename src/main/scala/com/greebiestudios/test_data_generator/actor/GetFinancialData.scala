package com.greebiestudios.test_data_generator.actor
import com.greebiestudios.test_data_generator.data.DataSource
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.checkerframework.checker.units.qual.s
import com.greebiestudios.test_data_generator.data.SODADataCaseClasses

object GetFinancialData {

  sealed trait GetFinancialAction
  final case class GetFinancialData(source: DataSource, url: String)
      extends GetFinancialAction
  final case class GetHousingData(source: DataSource, url: String)
      extends GetFinancialAction

  def apply(): Behavior[GetFinancialAction] = {
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case GetFinancialData(source, url) => 
          context.log.info("GetFinancialData received")
          val soda = new SODADataCaseClasses()
          soda.getAllBudgetData()
          Behaviors.ignore
          // TODO: Get data from source using url
          
        case GetHousingData(source, url) =>
          context.log.info("GetHousingData received")
          // TODO: Get data from source using url
          Behaviors.same
      }
    }
  }
}
