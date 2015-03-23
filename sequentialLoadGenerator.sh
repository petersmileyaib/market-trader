#!/bin/bash

# Infinite generation of the request for the trade

while :
do
	counter = expr counter + 1
	curl \
  --header "Content-type: application/json" \
  --request POST \
  --data "{\"userId\": \"134256\", \"currencyFrom\": \"EUR\", \"currencyTo\": \"GBP\",
	\"amountSell\": 1000, \"amountBuy\": 747.10, \"rate\": $RANDOM, \"timePlaced\" :
\"24-JAN-15 10:27:44\", \"originatingCountry\" : \"FR\"}" \
  http://localhost:9000/consumeTrade

done
