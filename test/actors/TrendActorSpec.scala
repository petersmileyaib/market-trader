package actors

import akka.actor._
import akka.testkit._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions

import scala.concurrent.duration._

import scala.collection.JavaConverters._
import play.api.test.WithApplication
import org.specs2.matcher.JsonMatchers

class TrendActorSpec extends TestkitExample with SpecificationLike with JsonMatchers with NoTimeConversions {

  /*
   * Running tests in parallel (which would ordinarily be the default) will work only if no
   * shared resources are used (e.g. top-level actors with the same name or the
   * system.eventStream).
   *
   * It's usually safer to run the tests sequentially.
   */

  sequential

  "TrendActor" should {    

    "send update to socket when receiving a CurrencyTrace message" in new WithApplication {
      val out = new StubOut()
      
      val fromCurrency = "EUR"
      val toCurrency = "GBP"
      val rate = 1234.0

      val userActorRef = TestActorRef[TrendActor](Props(new TrendActor(out)))
      val userActor = userActorRef.underlyingActor

      // send off the currency trade update...
      userActor.receive(CurrencyTrade(fromCurrency, toCurrency, rate))

      // ...and expect it to be a JSON node.
      val node = out.actual.toString
//      System.out.println("node: " + node)
      node must /("rate" -> rate)
      node must /("currencyPair" -> (fromCurrency + " to " + toCurrency))

    }

  }

}
