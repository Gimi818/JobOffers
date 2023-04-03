package com.joboffers.domain.offer.dto;

import lombok.Builder;

@Builder
public record JobOfferResponse(
        String companyName,
        String position,
        String salary,
        String offerUrl
) {
}
