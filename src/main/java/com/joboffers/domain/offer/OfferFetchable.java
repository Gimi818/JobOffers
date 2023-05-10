package com.joboffers.domain.offer;

import com.joboffers.domain.offer.dto.JobOfferResponse;

import java.util.List;

public interface OfferFetchable {

    List<JobOfferResponse> fetchOffers();
}
