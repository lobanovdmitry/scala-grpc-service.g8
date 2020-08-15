package ru.dlobanov.api.greeter

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GreeterSpec extends AnyFlatSpec with Matchers {

  behavior of "Greeter"

  "it" should "work" in {
    HelloRequest("Dmitry")
  }

}
