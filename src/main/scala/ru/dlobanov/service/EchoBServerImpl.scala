package ru.dlobanov.service

import ru.dlobanov.service.a.api.service_b.{EchoBServiceGrpc, EchoRequest, EchoResponse}

import scala.concurrent.Future

class EchoBServerImpl extends EchoBServiceGrpc.EchoBService {
  override def echo(request: EchoRequest) = {
    Future.successful(EchoResponse(s"HelloB, ${request.message}!"))
  }
}
