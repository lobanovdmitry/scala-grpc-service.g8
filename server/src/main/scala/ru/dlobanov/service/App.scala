package ru.dlobanov.service

object App {

  import scala.concurrent.ExecutionContext.Implicits._

  def main(args: Array[String]): Unit = {
    new GrpcServer(new GreeterImpl())
      .start(port = 50051)
      .blockUntilShutdown()
  }
}
