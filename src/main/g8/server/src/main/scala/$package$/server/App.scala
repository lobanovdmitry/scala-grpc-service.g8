package $package$.server

import scala.concurrent.ExecutionContext

object App {

  def main(args: Array[String]): Unit = {
    implicit val ec = ExecutionContext.global
    new GrpcServer(new GreeterImpl())
      .start(port = 50051)
      .blockUntilShutdown()
  }
}
