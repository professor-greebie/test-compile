package com.greebiestudios.test_data_generator.data

class ApiSourceInformation {
    val ckanApiBoilerplate = "/api/3/action/package_show"
    val torontoBaseUrl = "https://ckan0.cf.opendata.inter.prod-toronto.ca"
    val packageList = Seq("budget-operating-budget-program-summary-by-expenditure-category")

    def getResources(url: String): String = {
        packageList.foreach((x) { 
            val params = Map("id" -> x)
            val sourcePackage = Http(url).params(params).json()
            sourcePackage.get("result").get("resources")
              .foreach((x) => println(x.get("url").getAsString()))        
        })
    }

    
  
}
