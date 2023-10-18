package com.greebiestudios.test_data_generator.client

import com.greebiestudios.test_data_generator.data.DataInput
import org.checkerframework.checker.units.qual.t

object FederalDataFiles {
  val baseUrl = "https://www150.statcan.gc.ca/n1/tbl/csv/"
  val vacancyRatesResource = "34100130-eng.zip"
  val averageRentsResource = "34100133-eng.zip"

  val vacancyRates = DataInput(baseUrl, Seq(vacancyRatesResource, averageRentsResource))

}
