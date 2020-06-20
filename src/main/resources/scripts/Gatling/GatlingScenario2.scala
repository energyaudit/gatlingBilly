package com.baeldung

import scala.util._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class RewardsScenario2 extends Simulation {

  def randCustId() = Random.nextInt(99)
  
  val httpProtocol = http.baseUrl("http://localhost:8080")
					    .acceptHeader("text/html,application/json;q=0.9,*/*;q=0.8")
						.doNotTrackHeader("1")
						.acceptLanguageHeader("en-US,en;q=0.5")
						.acceptEncodingHeader("gzip, deflate")
						.userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
  
  val scn = scenario("RewardsScenario2")
	.repeat(3){
		exec(http("transactions_add")
		  .post("/posts/")
		  .body(StringBody("""{ "customerRewardsId":null,"customerId":""""+ randCustId() + """","transactionDate":null }""")).asJson
		.check(jsonPath("$.id").saveAs("txnId"))
		.check(jsonPath("$.transactionDate").saveAs("txtDate"))
		.check(jsonPath("$.customerId").saveAs("custId")))
		.pause(1)
		
		.exec(http("get_reward1")
		  .get("/posts/${custId}")
		  .check(jsonPath("$.id").saveAs("rwdId")))
		.pause(1)
		
		.doIf("${rwdId.isUndefined()}"){
			exec(http("rewards_add")
			  .post("/posts")
			  .body(StringBody("""{ "customerId": "${custId}" }""")).asJson
			.check(jsonPath("$.id").saveAs("rwdId")))
		}
		
		.exec(http("transactions_add")
		  .post("/posts")
		  .body(StringBody("""{ "customerRewardsId":"${rwdId}","customerId":"${custId}","transactionDate":"${txtDate}" }""")).asJson)
		.pause(1)
		
		.exec(http("get_reward2")
		  .get("/posts/${rwdId}"))
	}
  setUp(
    scn.inject(atOnceUsers(2))
  ).protocols(httpProtocol)
}