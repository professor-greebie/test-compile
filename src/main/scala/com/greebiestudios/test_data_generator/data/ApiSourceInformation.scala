package com.greebiestudios.test_data_generator.data


class ApiSourceInformation {
    val ckanApiBoilerplate = "/api/3/action/package_show"
    val torontoBaseUrl = "https://ckan0.cf.opendata.inter.prod-toronto.ca"
    val packageList = Seq("budget-operating-budget-program-summary-by-expenditure-category")

    def getResources(url: String):String = {

        val response = scala.io.Source.fromURL("https://ckan0.cf.opendata.inter.prod-toronto.ca/api/3/actoin/package_show")
            .mkString
        response
    }

    
  
}
