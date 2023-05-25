package com.greebiestudios.test_data_generator.web

import com.greebiestudios.test_data_generator.data.Identity
import akka.stream.scaladsl.Source
import akka.actor.Cancellable
import concurrent.duration.DurationInt
import com.greebiestudios.test_data_generator.data.NameGenerator
import spray.json.JsString
import scala.io.Codec
import java.nio.charset.CodingErrorAction

object NameStreams
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {
  val l10nCodes = Seq("en-IN", "en-CA", "en-US", "ar-SA", "ko-KR", "zh-TW")

  lazy val (firstNames, lastNames) = getData()
  def getData() = {
    var names: Seq[(Seq[String], Seq[String])] =
      Seq[(Seq[String], Seq[String])]()
    for name: String <- l10nCodes do names = names :+ pullFromResource(name)
    //names.foreach((x) => println(x))
    val firstNamesList: Seq[String] = names.flatMap((first, last) => first)
    val lastNamesList: Seq[String] = names.flatMap((first, last) => last)
    // print(firstNamesList(0))
    (firstNamesList, lastNamesList)
  }

  def pullFromResource(folder: String) = {
    val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.IGNORE)
    val firstNames: Seq[String] = scala.io.Source
      .fromResource("l10n/" + folder + "/first_names.txt")(decoder)
      .getLines()
      .toSeq
    val lastNames: Seq[String] = scala.io.Source
      .fromResource("l10n/" + folder + "/last_names.txt")(decoder)
      .getLines()
      .toSeq
    // print(lastNames(0))
    (firstNames, lastNames)
  }

  def tickStream(fun: () => Identity): Source[Identity, Cancellable] = {
    val source: Source[Identity, Cancellable] = Source
      .tick[Identity](250.millis, 500.millis, Identity("", ""))
      .map(_ => fun())
    source
  }

  def getNames(seed: Option[Int] = None): Source[Identity, Cancellable] = {
    val genfirst = NameGenerator(firstNames)
    genfirst.seed = seed
    val genLast = NameGenerator(lastNames)
    genLast.seed = seed
    tickStream(() => Identity(genfirst.generate(), genLast.generate()))
  }
}
