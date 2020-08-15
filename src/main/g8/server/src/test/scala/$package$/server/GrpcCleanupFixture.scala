package $package$.server

import java.util.concurrent.TimeUnit

import com.google.common.base.{Stopwatch, Ticker}
import io.grpc.{ManagedChannel, Server}

import scala.collection._

/**
 * Fixture that can register gRPC resources and manages its automatic release at
 * the end of the test. If any of the resources registered can not be successfully
 * released, the test will fail.
 * <p>Ported from GrpcCleanupRule (JUnit-rule)
 * https://github.com/grpc/grpc-java/blob/master/testing/src/main/java/io/grpc/testing/GrpcCleanupRule.java.
 *
 */
trait GrpcCleanupFixture {

  private val resources = mutable.ListBuffer.empty[Resource]

  private var stopwatch = Stopwatch.createUnstarted
  private var timeoutNanos = TimeUnit.SECONDS.toNanos(10)
  private var firstException: Throwable = _

  /**
   * Sets a positive total time limit for the automatic resource cleanup. If any of the resources
   * registered fails to be released in time, the test will fail.
   *
   * @return this
   */
  def setTimeout(timeout: Long, timeUnit: TimeUnit): GrpcCleanupFixture = {
    require(timeout > 0, "timeout should be positive")
    timeoutNanos = timeUnit.toNanos(timeout)
    this
  }

  /**
   * Sets a specified time source for monitoring cleanup timeout.
   *
   * @return this
   */
  def setTicker(ticker: Ticker): GrpcCleanupFixture = {
    stopwatch = Stopwatch.createUnstarted(ticker)
    this
  }

  /**
   * Registers the given channel to the fixture. Once registered, the channel will be automatically
   * shutdown at the end of the test.
   *
   * <p>This method need be properly synchronized if used in multiple threads. This method must
   * not be used during the test teardown.
   *
   * @return the input channel
   */
  def registerForCleanup(channel: ManagedChannel): ManagedChannel = {
    require(channel != null, "channel should not be null")
    register(new ManagedChannelResource(channel))
    channel
  }

  /**
   * Registers the given server to the fixture. Once registered, the server will be automatically
   * shutdown at the end of the test.
   *
   * <p>This method need be properly synchronized if used in multiple threads. This method must
   * not be used during the test teardown.
   *
   * @return the input server
   */
  def registerForCleanup(server: Server): Server = {
    require(server != null, "server should not be null")
    register(new ServerResource(server))
    server
  }

  private def register(resource: Resource): Unit = {
    resources += resource
  }

  def withCleanup(testCode: GrpcCleanupFixture => Unit): Unit = {
    try {
      testCode.apply(this)
    } catch {
      case e: Throwable =>
        firstException = e
        try {
          teardown()
        } catch {
          case e2: Throwable => e.addSuppressed(e2)
        }
        throw e
    }

    teardown()
    if (firstException != null) throw firstException
  }

  /**
   * Releases all the registered resources.
   */
  private def teardown(): Unit = {
    stopwatch.start()

    if (firstException == null)
      resources.reverse.foreach(_.cleanUp())

    resources.reverse.foreach { resource =>
      if (firstException != null) {
        resource.forceCleanUp()
      } else {
        try {
          val timeout = timeoutNanos - stopwatch.elapsed(TimeUnit.NANOSECONDS)
          val released = resource.awaitReleased(timeout, TimeUnit.NANOSECONDS)
          if (!released) {
            firstException = new AssertionError(s"Resource \$resource can not be released in time at the end of test")
          }
        } catch {
          case e: InterruptedException =>
            Thread.currentThread().interrupt()
            firstException = e
          case e: Throwable => throw e
        }
        if (firstException != null) resource.forceCleanUp()
      }
    }

    resources.clear()
  }

  trait Resource {

    def cleanUp(): Unit

    /**
     * Error already happened, try the best to clean up. Never throws.
     */
    def forceCleanUp(): Unit

    /**
     * Returns true if the resource is released in time.
     */
    def awaitReleased(duration: Long, timeUnit: TimeUnit): Boolean
  }

  class ManagedChannelResource(private val channel: ManagedChannel) extends Resource {

    override def cleanUp(): Unit = channel.shutdown

    override def forceCleanUp(): Unit = channel.shutdownNow

    override def awaitReleased(duration: Long, timeUnit: TimeUnit): Boolean = {
      channel.awaitTermination(duration, timeUnit)
    }

    override def toString: String = channel.toString
  }

  class ServerResource(private val server: Server) extends Resource {

    override def cleanUp(): Unit = server.shutdown

    override def forceCleanUp(): Unit = server.shutdownNow

    override def awaitReleased(duration: Long, timeUnit: TimeUnit): Boolean = {
      server.awaitTermination(duration, timeUnit)
    }

    override def toString: String = server.toString
  }

}
