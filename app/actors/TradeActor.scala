package actors

import akka.actor.Actor
import akka.event.Logging
import akka.actor.Props
import play.libs.Akka
import akka.actor.ActorRef
import scala.collection.immutable.{HashSet, Queue}
 
case class CurrencyTrade(fromCurrency: String, toCurrency: String, rate: Double)

case class WatchCurrencyPairs(currencyPair: Option[String])

case class UnwatchCurrencyPairs(currencyPair: Option[String])

object TradeActor {
  lazy val tradeActor: ActorRef = Akka.system.actorOf(Props(classOf[TradeActor]))
}

class TradeActor extends Actor {
  
  val log = Logging(context.system, this)
  
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  
  def receive = {
    
    // main event processed and broadcast onwards to any watchers
    case CurrencyTrade(fromCurrency, toCurrency, rate) => {
      
      log.info("received trade info: from -> " + fromCurrency + ", to -> " + toCurrency + ", rate -> " + rate)
      
      watchers.foreach(_ ! CurrencyTrade(fromCurrency, toCurrency, rate))
    }
    
    case WatchCurrencyPairs(_) => {
      
      // Add sender to list of watchers of CurrencyTrade events
      watchers = watchers + sender
    }
    
    case UnwatchCurrencyPairs(_) => {
      
      // unsubscribe from receiving these events
      watchers = watchers - sender
      if (watchers.size == 0) {
        
        context.stop(self)
      }
    }
    
    case _ => {
      
      log.error("have received unhandled message!!")
    }

  }
  
}



