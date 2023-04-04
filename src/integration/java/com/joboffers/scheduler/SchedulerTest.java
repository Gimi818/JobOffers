package com.joboffers.scheduler;

import com.joboffers.BaseIntegrationTests;
import com.joboffers.OffersSpringBootApplication;
import com.joboffers.domain.offer.OfferFetchable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.Duration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
@SpringBootTest(classes = OffersSpringBootApplication.class, properties = "scheduling.enabled=true")
public class SchedulerTest  extends BaseIntegrationTests {

    @SpyBean
    OfferFetchable remoteOfferClient;

    @Test
    public void should_run_http_client_offers_fetching_exactly_given_times() {
        await().
                atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(remoteOfferClient, times(1 )).fetchOffers());
    }
}
