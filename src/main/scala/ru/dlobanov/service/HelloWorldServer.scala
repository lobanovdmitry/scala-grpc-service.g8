package ru.dlobanov.service

import java.util.logging.Logger

import io.grpc.{Server, ServerBuilder}
import ru.dlobanov.service.a.api.service_b.EchoBServiceGrpc
import ru.dlobanov.service.b.api.service_a.EchoAServiceGrpc

import scala.concurrent.ExecutionContext

object HelloWorldServer {
  private val logger = Logger.getLogger(classOf[HelloWorldServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }

  private val port = 50051
}

class HelloWorldServer(executionContext: ExecutionContext) {
  self =>
  private[this] var server: Server = null

  private def start(): Unit = {

    server = ServerBuilder
      .forPort(HelloWorldServer.port)
      .addService(EchoAServiceGrpc.bindService(new EchoAServerImpl(), executionContext)).asInstanceOf[ServerBuilder[_]]
      .addService(EchoBServiceGrpc.bindService(new EchoBServerImpl(), executionContext)).asInstanceOf[ServerBuilder[_]]
      .build.start
    HelloWorldServer.logger.info("Server started, listening on " + HelloWorldServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

}
