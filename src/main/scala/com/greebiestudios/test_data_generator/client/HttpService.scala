package com.greebiestudios.test_data_generator.client

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.HttpResponse
import scala.concurrent.Future
import com.greebiestudios.test_data_generator.actor.RunSystem
import com.greebiestudios.test_data_generator.actor.RootActorSystem
import scala.concurrent.ExecutionContext
import akka.stream.scaladsl.Source
import akka.NotUsed
import akka.util.ByteString
import com.greebiestudios.test_data_generator.data.DataInput
import akka.stream.scaladsl.FileIO
import java.nio.file.Paths

trait HttpService (using val input: DataInput) {
    implicit val system: akka.actor.typed.ActorSystem[RunSystem.SystemRunning] =
    RootActorSystem()
    implicit val ec: ExecutionContext = system.executionContext

    val tempDir = Paths.get(System.getProperty("java.io.tmpdir"))

    private def getResource(url: String, resource: String):Future[HttpResponse] = {
        val http = Http()
        val uri: Uri = Uri(url + resource)
        val request = HttpRequest(uri = uri)
        system.log.info("Requesting: " + url + resource)
        http.singleRequest(request)
    }
    
    def getByteString():Source[Source[ByteString, Any], NotUsed] = {
        val result = input.resource
          .map(resp => getResource(input.url, resp))
        Source(result).mapAsync(2)(item => item).map(item => item.entity.dataBytes)
        .log("Info", item => item.toString)
    }

    def saveToFile() = {
        val result: Source[String, NotUsed] = Source(input.resource)
          .mapAsync(2)(name => { 
            val get = getResource(input.url, name)
              .flatMap(resp => 
              resp.entity.dataBytes.runWith(FileIO.toPath(Paths.get(name)))).map(_ => name)
            get
          })
        result
    }
  
}
