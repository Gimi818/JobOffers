package com.joboffers.domain.offer;

import com.joboffers.domain.offer.exception.DuplicateException;
import com.joboffers.domain.offer.exception.SavingException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static com.joboffers.domain.offer.OfferMapper.mapper;

@AllArgsConstructor
public class OfferService {

    private final OfferFetchable flatOfferFetchable;
    private final OfferRepository OfferRepository;

    List<Offer> fetchAllOffersAndSaveAllIfNotExists() {
        List<Offer> jobOffers = fetchOffers();
        final List<Offer> offers = filterNotExistingOffers(jobOffers);
        try {

            return OfferRepository.saveAll(offers);
        } catch (DuplicateException duplicateKeyException) {
            throw new SavingException(duplicateKeyException.getMessage(), jobOffers);
        }
    }

    private List<Offer> filterNotExistingOffers(List<Offer> Offers) {
        return Offers.stream()
                .filter(offerDto -> !offerDto.offerUrl().isEmpty())
                .filter(offerDto -> !OfferRepository.existsByOfferUrl(offerDto.offerUrl()))
                .collect(Collectors.toList());
    }

    private List<Offer> fetchOffers() {
        return flatOfferFetchable.fetchOffers()
                .stream()
                .map(mapper::mapFromOfferResponseToOffer)
                .toList();
    }

}
