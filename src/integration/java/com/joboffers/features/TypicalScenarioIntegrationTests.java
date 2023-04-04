package com.joboffers.features;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.joboffers.BaseIntegrationTests;
import com.joboffers.SampleJobOffersResponse;
import com.joboffers.infrastructure.scheduler.HttpScheduler;
import com.joboffers.scheduler.SchedulerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


public class TypicalScenarioIntegrationTests extends BaseIntegrationTests implements SampleJobOffersResponse {
    @Autowired
    HttpScheduler httpScheduler;

    @Test
    public void user_want_to_see_offers_but_have_to_be_logged_in_and_external_server_should_have_some_offers() {
        //step 1: there are no offers in external HTTP server
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithFourOffersJson())));
        httpScheduler.fetchAllOffersAndSaveAllIfNotExists();
    }
}
