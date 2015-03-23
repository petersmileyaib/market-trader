package actors

import akka.actor._
import akka.testkit._

import org.specs2.mutable._
import org.specs2.time.NoTimeConversions

import scala.concurrent.duration._
import scala.collection.immutable.HashSet


class TradeActorSpec extends TestkitExample with SpecificationLike with NoTimeConversions {

  /*
   * Running tests in parallel (which would ordinarily be the default) will work only if no
   * shared resources are used (e.g. top-level actors with the same name or the
   * system.eventStream).
   *
   * It's usually safer to run the tests sequentially.
   */
  sequential

  final class TradeActorWithWatcher(watcher: ActorRef) extends TradeActor() {
    watchers = HashSet[ActorRef](watcher)

  }

  "A TradeActor" should {

    "notify watchers when a new trade is received" in {
      // Create a trend actor with a stubbed out TradeActor actor
      val probe = new TestProbe(system)
      
      val fromCurrency = "EUR"
      val toCurrency = "GBP"
      val rate = 1234.0
      val tradeActor = system.actorOf(Props(new TradeActorWithWatcher(probe.ref)))

      system.actorOf(Props(new ProbeWrapper(probe)))

      // Fire off the message...
      tradeActor ! new CurrencyTrade(fromCurrency, toCurrency, rate)

      // ... and ask the probe if it got the trade message.
      val actualMessage = probe.receiveOne(500 millis)
      val expectedMessage = CurrencyTrade(fromCurrency, toCurrency, rate)
      
      actualMessage must ===(expectedMessage)
    }

  }
}
