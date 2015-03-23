package actors;

import play.Logger;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.Option;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The broker between the WebSocket and the TradeActor.  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */

public class TrendActor extends UntypedActor {
	
    private final WebSocket.Out<JsonNode> out;
    
    public TrendActor(WebSocket.Out<JsonNode> out) {
        this.out = out;
        
        final Option<String> none = Option.empty();
        TradeActor.tradeActor().tell(new WatchCurrencyPairs(none), getSelf());       
    }
    
    public void onReceive(Object message) {
    	
    	Logger.info("have received a message: " + message.getClass());
    	
    	if(message instanceof CurrencyTrade) {
    		
    		Logger.info("have received a CurrencyTrade message");
    		
    		CurrencyTrade currencyTrade = (CurrencyTrade)message;
    		
    		ObjectNode currencyTradeMessage = Json.newObject();
    		currencyTradeMessage.put("currencyPair", currencyTrade.fromCurrency() + " to " + currencyTrade.toCurrency());
    		currencyTradeMessage.put("rate", currencyTrade.rate());
    		
    		out.write(currencyTradeMessage);
    		
    	} 
    }
}
