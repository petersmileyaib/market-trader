Market Trader
================

Message Consumption
-----------------------

I spent the majority of my time focusing on the message consumption. 
Am using Scala and Java with the Play framework and AKKA actors to process.
I haven't used Scala in a production environment before, but am currently working with the older Play framework which uses Java only and am interested in some of the techniques applied for scaling and thought this would be a good opportunity to use it.

The consumer endpoint is asynchronous in that will hold open the connection until the Message Processor Future completes, but is non-blocking of further web server requests.
The endpoint converts from the incoming JSON to a Trade object, and then the processMessage is called as part of the Message Processing component.
Would normally have some sort of security here, i.e. HTTPS only connections plus either a secure session through whatever login means are used or use JSON Web Token (JWT) in the Authorization header if its being used from another process.
The conversion process implies a certain low level validation, but would need to either have an XSS filter to strip out potential attacks plus field level range and usage validation also.
In terms of scaling, I haven't put anything specific in the code to limit the message consumption, but could use an auto scaling group in Amazon to allow for greater throughput once had setup some plumbing for the AKKA actors to register themselves with some locator service.

Message Processor
----------------------
Primarily this is where any business logic of processing the message would lie in terms of verifying the permissions of the user to perform the action, persisting the data to a store, auditing the action and broadcasting any related events, in this case specifically allowing any watcher AKKA actors know that a new trade has occurred, which brings us on to the UI for visualizing.

Trend UI
-----------------------
This uses a WebSocket setup through Java that subscribes through AKKA actors to any trade events and streams these events to the UI as they come.
The UI component only has one chart to represent a from and to currency, but could have multiple charts plus could sub-divide further by region etc...
	

Process Flow
-----------------
Incoming trade message processed by TradeController.consumeTrade -> Trade.processMessage -> Returns result and sends currencytrade event to TradeActor
TradeActor -> Sends currency trades to any registered watchers
TrendController -> TrendActor -> TradeActor registers watcher to be able to stream trade events to the WebSocket

Build
---------------
Using the activator framework version 1.3.2.
If running locally, just run the activator.bat or activator script and can run in web ui.
Requires JDK6 or higher with associated env vars set.

Testing
-----------------
Used the following curl for single tests:
curl \
  --header "Content-type: application/json" \
  --request POST \
  --data '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP",
"amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" :
"24-JAN-15 10:27:44", "originatingCountry" : "FR"}' \
  http://localhost:9000/consumeTrade

For sequential infinite variable trades to assist with the trend graphing used a bash script called sequentialLoadGenerator.sh

I benchmarked the performance on my local system using Apache bench (tool primarily aimed at stress testing web server throughput with little or no dynamic data differences).
Used following command to run it within the root of the project directory
ab -c 1 -n 1 -T "application/json" -p postBody.txt http://localhost:9000/consumeTrade 
