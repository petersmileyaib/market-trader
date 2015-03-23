package controllers

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api._
import play.api.Play
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.JsError
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.ws._
import play.api.mvc._
import play.api.mvc._
import domain.Trade
import domain.TradeProcessResult
import play.api.Logger

/**
 * Trade consumption endpoint for taking JSON requests for trades and processing
 * 
 * @author Peter Smiley
 */
object TradeController extends Controller {  
  
  // Conversion from incoming JSON to an instance of the Trade class
  implicit val tradeReads: Reads[Trade] = (
    (JsPath \ 'userId).read[String] and
    (JsPath \ 'currencyFrom).read[String] and
    (JsPath \ 'currencyTo).read[String] and
    (JsPath \ 'amountSell).read[BigDecimal] and
    (JsPath \ 'amountBuy).read[BigDecimal] and
    (JsPath \ 'rate).read[Double] and
    (JsPath \ 'timePlaced).read[String] and
    (JsPath \ 'originatingCountry).read[String] 
  )(Trade.apply _)
  
  
  /**
   * Endpoint for consuming incoming trade JSON messages.
   * Expected incoming format:
   *  {"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}
   *  
   * Expected outgoing format: 
   *  {"referenceId": "[transaction reference id]"}
   *  
   * Will timeout after a certain period
   * 
   *
   *  To test locally:
   *    curl \
   *     --header "Content-type: application/json" \
   *     --request POST \
   *     --data '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP",
   *     "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" :
   *     "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' \
   *     http://localhost:9000/consumeTrade
   */
  def consumeTrade = Action.async(BodyParsers.parse.json) { request =>
    
    Logger.debug("consuming trade...")
    
    request.body.validate[Trade].map { trade =>
      
      val tradeConsumptionResult = scala.concurrent.Future { 
        
        // TODO validate message contents
        // TODO prevent XSS etc...
        
        // process the message
        trade.processMessage 
        
      }
      
      val timeoutFuture = play.api.libs.concurrent.Promise.timeout("Oops", 60.second)
        
      // Async will process exclusively whichever of these completes first
      Future.firstCompletedOf(Seq(tradeConsumptionResult, timeoutFuture)).map {
        
        // Trade completed successfully, return 200 with a transaction reference id if it's available
        case tradeProcessResult: TradeProcessResult =>  {
          
          if(tradeProcessResult != null && tradeProcessResult.isSuccessful) {
            
            Ok(Json.obj("status" ->"OK", "referenceId" -> (trade.referenceId) ))
            
          } else {
            
            Ok(Json.obj("status" ->"NOK", "messagge" -> (tradeProcessResult.message) ))
          }
          
        }
        
        // timed out, return error status
        case t: String => Ok(Json.obj("status" ->"KO", "message" -> "TimedOut"))
        
      }
        
    }.recoverTotal { e => 
      
      // errors raised, return the error
      Future.successful(Ok(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(e))))
    }

  }  

}
