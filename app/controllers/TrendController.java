package controllers;

import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.Option;
import actors.TradeActor;
import actors.TrendActor;
import actors.UnwatchCurrencyPairs;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * The main web controller that handles returning the index page, setting up a WebSocket, and watching the currency trades.
 */
public class TrendController extends Controller {

    public static Result index() {
        return ok(views.html.trends.render());
    }

    public static WebSocket<JsonNode> ws() {
    	
        return new WebSocket<JsonNode>() {
        	
            public void onReady(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
            	
                // create a new TrendActor and get it to register with the TradeActor as a watcher
                final ActorRef trendActor = Akka.system().actorOf(Props.create(TrendActor.class, out));

                // on close, tell the trendActor to shutdown and unregister the tradeActor watcher
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                    	
                    	Logger.info("closing the connection");
                        final Option<String> none = Option.empty();
                        TradeActor.tradeActor().tell(new UnwatchCurrencyPairs(none), trendActor);
                        Akka.system().stop(trendActor);
                        
                    }
                });
            }
        };
    }

}
