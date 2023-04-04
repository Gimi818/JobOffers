package com.joboffers.infrastructure.offer.controller;

import com.joboffers.domain.offer.OfferFacade;
import com.joboffers.domain.offer.dto.OfferResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferFacade offerFacade;

    @GetMapping
    public ResponseEntity<List> findAllOffers() {
        List<OfferResponseDto> allOffers = offerFacade.findAllOffers();
        return ResponseEntity.ok().body(allOffers);


    }
}
