package com.greebiestudios.test_data_generator.data

class SODADataCaseClasses {

    val EdmontonBaseUrl = "https://data.edmonton.ca/resource/"
    val EdmontonBudgetDataUrl = "qycq-4ckj.json"
  case class EdmontonBudgetData(
      year: String,
      department_agencies: String,
      branch: String,
      status: String,
      budget_category: String,
      budget_sub_category: String,
      dollar_amount: String
  )

}
