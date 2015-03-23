package domain

import java.util.UUID
import akka.actor.actorRef2Scala
import actors.TradeActor
import actors.CurrencyTrade

class TradeProcessResult(val isSuccessful: Boolean, val message: String)

case class Trade(
    val userId: String, 
    val currencyFrom: String, 
    val currencyTo: String, 
    val amountSell: BigDecimal, 
    val amountBuy: BigDecimal, 
    val rate: Double, 
    val timePlaced: String, 
    val originationCountry: String) {

  // unique reference to this specific trade
  val referenceId: String = UUID.randomUUID().toString()

  def isValidTrade: Boolean = {
    
    // validate fields...
    
    true
  }
  
  def save: Boolean = {
    
    // save to db or SAN...
    
    true
  }

  def processMessage: TradeProcessResult = {
    
    // validate the message
    if(!isValidTrade) {
      
      return new TradeProcessResult(false, "Invalid payload...")
    }
    
    // TODO store it to a database or SAN...
    if(!save) {
      
      return new TradeProcessResult(false, "Unable to save because...")
    }
    
    // send it to an Actor to be streamed to a UI piece
    TradeActor.tradeActor ! new CurrencyTrade(currencyFrom, currencyTo, rate)
        
    new TradeProcessResult(true, null)
  }
}


