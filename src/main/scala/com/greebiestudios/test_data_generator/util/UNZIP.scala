package com.greebiestudios.test_data_generator.util

import java.util.zip.ZipInputStream
import akka.stream.scaladsl.Flow
import java.io.ByteArrayInputStream
import akka.util.ByteString
import akka.NotUsed
import com.greebiestudios.test_data_generator.actor.RunSystem
import com.greebiestudios.test_data_generator.actor.RootActorSystem
import scala.concurrent.ExecutionContext
import java.nio.charset.Charset
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import java.util.stream.Collectors
import akka.util.ccompat.JavaConverters.EnumerationHasAsScala

class UNZIP {
  implicit val system: akka.actor.typed.ActorSystem[RunSystem.SystemRunning] =
    RootActorSystem()
  implicit val ec: ExecutionContext = system.executionContext

  // Avoid Zip Bombs
  val MAX_SIZE = 999999999999L
  val unZipFLOW = Flow[String]
    .mapConcat(filepath => {
      val zipFile = new ZipFile(filepath)
      val zipEntries = zipFile.entries().asScala.toList
      zipEntries.map(entry => {
        system.log.info("Processing: " + entry.getName())
        system.log.info("Size: " + entry.getSize())
        val entryStream = zipFile.getInputStream(entry)
        val entryBytes = ByteString(entryStream.readAllBytes())
        entryStream.close()
        entryBytes
      })
    })   
}
