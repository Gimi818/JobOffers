package com.joboffers.domain.offer.exception;

import lombok.Getter;

@Getter
public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(String offerId) {
        super(String.format("Offer with id %s not found", offerId));

    }
}
