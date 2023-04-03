package com.joboffers.domain.offer;

import com.joboffers.domain.offer.dto.JobOfferResponse;

import java.util.List;

public class OfferFacadeTestConfiguration {

    private final InMemoryFetcherTestImpl inMemoryFetcherTest;
    private final InMemoryOfferRepository offerRepository;

    OfferFacadeTestConfiguration() {
        this.inMemoryFetcherTest = new InMemoryFetcherTestImpl(
                List.of(
                        new JobOfferResponse("Google", "Junior", "7999", "1"),
                        new JobOfferResponse("Amazon", "Senior", "21000", "2"),
                        new JobOfferResponse("Comarch", "Mid", "12000", "3"),
                        new JobOfferResponse("Finanteq", "Junior", "5000", "4"),
                        new JobOfferResponse("MBank", "Junior", "8000", "5"),
                        new JobOfferResponse("PKO", "Senior", "20000", "6")
                )
        );
        this.offerRepository = new InMemoryOfferRepository();
    }

    OfferFacadeTestConfiguration(List<JobOfferResponse> remoteClientOffers) {
        this.inMemoryFetcherTest = new InMemoryFetcherTestImpl(remoteClientOffers);
        this.offerRepository = new InMemoryOfferRepository();
    }

    OfferFacade offerFacadeForTests() {
        return new OfferFacade(offerRepository, new OfferService(inMemoryFetcherTest, offerRepository));
    }
}
