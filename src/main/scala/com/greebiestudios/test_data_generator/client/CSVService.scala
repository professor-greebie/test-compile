package com.greebiestudios.test_data_generator.client

import akka.stream.scaladsl.Flow
import akka.util.ByteString
import akka.NotUsed
import akka.stream.alpakka.csv.scaladsl.CsvParsing.{Comma, DoubleQuote, Backslash}
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.csv.scaladsl.CsvToMap
import java.nio.charset.StandardCharsets

object CSVService {
  val delimeter = Comma
  val quoteChar = DoubleQuote
  val escapeChar = Backslash
  val csvFlow: Flow[ByteString, List[ByteString], NotUsed] = CsvParsing.lineScanner(delimeter, quoteChar, escapeChar)
  val csvToMap: Flow[List[ByteString], Map[String, String], NotUsed] = CsvToMap.toMapAsStrings(StandardCharsets.UTF_8)
}
