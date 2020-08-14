package ru.dlobanov.service

import ru.dlobanov.service.b.api.service_a.{EchoAServiceGrpc, EchoRequest, EchoResponse}

import scala.concurrent.Future

class EchoAServerImpl extends EchoAServiceGrpc.EchoAService {
  override def echo(request: EchoRequest) = {
    Future.successful(EchoResponse(s"HelloA, ${request.message}!"))
  }
}
