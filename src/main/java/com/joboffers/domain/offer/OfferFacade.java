package com.joboffers.domain.offer;

import com.joboffers.domain.offer.dto.OfferRequestDto;
import com.joboffers.domain.offer.dto.OfferResponseDto;
import com.joboffers.domain.offer.exception.NotFoundException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static com.joboffers.domain.offer.OfferMapper.mapper;

@AllArgsConstructor

public class OfferFacade {

    private final OfferRepository offerRepository;
    private final OfferService offerService;

    public List<OfferResponseDto> findAllOffers() {
        return offerRepository.findAll()
                .stream()
                .map(mapper::mapFromOfferToDto)
                .collect(Collectors.toList());
    }

    public List<OfferResponseDto> fetchAllOffersAndSaveAllIfNotExists() {
        return offerService.fetchAllOffersAndSaveAllIfNotExists()
                .stream()
                .map(mapper::mapFromOfferToDto)
                .toList();
    }

    public OfferResponseDto findOfferById(String id) {
        return offerRepository.findById(id)
                .map(mapper::mapFromOfferToDto)
                .orElseThrow(() -> new NotFoundException(id));
    }

    public OfferResponseDto saveOffer(OfferRequestDto offerDto) {
        final Offer offer = mapper.mapFromOfferDtoToOffer(offerDto);
        final Offer save = offerRepository.save(offer);
        return mapper.mapFromOfferToDto(save);
    }
}
