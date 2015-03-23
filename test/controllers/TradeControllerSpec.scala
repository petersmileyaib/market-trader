package controllers

import play.api.libs.json._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.mock.Mockito
import play.api.mvc.Action
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.JsValue
import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class TrendControllerSpec extends Specification {

//  val unit = new Application()

  "TradeController " should {

    "submit a new trade to the consumer endpoint, /consumeTrade" in {
      running(FakeApplication()) {

        val requestJson = Json.parse(""" 
          {
            "userId": "134256", 
            "currencyFrom": "EUR", 
            "currencyTo": "GBP", 
            "amountSell": 1000, 
            "amountBuy": 747.10, 
            "rate": 0.7471, 
            "timePlaced" : "24-JAN-15 10:27:44", 
            "originatingCountry" : "FR"
          }""")

        val Some(result) = route(
          FakeRequest(
            POST,
            "/consumeTrade",
            FakeHeaders(Seq("Content-type" -> Seq("application/json"))),
            requestJson))

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")

        val json = Json.parse(contentAsString(result))
    
        val statusCode = (json \ "status").asOpt[String]
        val referenceId = (json \ "referenceId").asOpt[String]
        
        statusCode must beSome("OK")
        referenceId must beSome
      }
    }
  }

}