package $package$.server

import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import $package$.api.greeter.GreeterGrpc.{GreeterBlockingStub, GreeterStub}
import $package$.api.greeter.{GreeterGrpc, HelloReply, HelloRequest}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class GreeterImplSpec extends AnyFlatSpec with Matchers {

  implicit val ec = ExecutionContext.global

  val serverName = InProcessServerBuilder.generateName()
  val serverBuilder = InProcessServerBuilder.forName(serverName).directExecutor()
  val channelBuilder = InProcessChannelBuilder.forName(serverName).directExecutor()
  val greeterService = new GreeterImpl()
  val service = GreeterGrpc.bindService(greeterService, ec)

  def cleanupFixture() = new GrpcCleanupFixture {
    val server = registerForCleanup(
      serverBuilder.addService(service).build().start()
    )
    val channel = registerForCleanup(
      channelBuilder.maxInboundMessageSize(1024).build()
    )
  }

  def withClient(testCode: GreeterStub => Unit): Unit = {
    val fixture = cleanupFixture()
    fixture.withCleanup { _ =>
      testCode.apply(GreeterGrpc.stub(fixture.channel))
    }
  }

  def withBlockingClient(testCode: GreeterBlockingStub => Unit): Unit = {
    val fixture = cleanupFixture()
    fixture.withCleanup { _ =>
      testCode.apply(GreeterGrpc.blockingStub(fixture.channel))
    }
  }

  behavior of "GreeterImpl"

  it should "reply on SayHello sync" in withBlockingClient { client =>
    val request = HelloRequest("man")

    // Business method
    val reply: HelloReply = client.sayHello(request)

    // Asserts
    reply shouldBe HelloReply("Hello, man!")
  }

  it should "reply on SayHello async" in withClient { client =>
    val request = HelloRequest("man")

    // Business method
    val reply: Future[HelloReply] = client.sayHello(request)

    // Asserts
    Await.result(reply, 1.second) shouldBe HelloReply("Hello, man!")
  }
}
