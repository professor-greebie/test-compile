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
