package com.greebiestudios.test_data_generator.data

trait DataOutput
case class DefaultDataOutput(
    key: String,
    url: String,
    data: String
) extends DataOutput

trait DataCaseClassTrait {
  def getDataOutput: DataOutput = {
    DefaultDataOutput("test", "test", "test")
  }
}

case class BudgetData (
    city: String,
    year: String,
    department: String,
    branch: String,
    status: String,
    category: String,
    subcategory: String,
    notes: String,
    adopted: String,
    amended: String,
    available: String,
    spent: String,
  ) extends DataCaseClassTrait {
    override def getDataOutput: DataOutput = {
      DefaultDataOutput("test", "test", "test")
    }
  }
