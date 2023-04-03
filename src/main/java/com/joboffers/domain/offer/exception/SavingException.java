package com.joboffers.domain.offer.exception;

import com.joboffers.domain.offer.Offer;
import lombok.Getter;

import java.util.List;

@Getter

public class SavingException extends RuntimeException{
    private final List<String> offerUrls;

    public SavingException(String offerUrl) {
        super(String.format("Offer with offerUrl [%s] already exists", offerUrl));
        this.offerUrls = List.of(offerUrl);
    }

    public SavingException(String message, List<Offer> offers) {
        super(String.format("error" + message + offers.toString()));
        this.offerUrls = offers.stream().map(Offer::offerUrl).toList();
    }

}
