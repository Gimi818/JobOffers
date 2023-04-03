package com.joboffers.domain.offer.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {


    private final String flatOfferId;


    public NotFoundException(String flatOfferId) {
        super(String.format("Flat offer with id %s not found", flatOfferId));
        this.flatOfferId = flatOfferId;
    }
}
