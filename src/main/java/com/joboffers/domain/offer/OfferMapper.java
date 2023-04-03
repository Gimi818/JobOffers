package com.joboffers.domain.offer;

import com.joboffers.domain.offer.dto.JobOfferResponse;
import com.joboffers.domain.offer.dto.OfferRequestDto;
import com.joboffers.domain.offer.dto.OfferResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OfferMapper {

    OfferMapper mapper = Mappers.getMapper(OfferMapper.class);

    OfferResponseDto mapFromOfferToDto(Offer offers);

    Offer mapFromOfferDtoToOffer(OfferRequestDto offerRequestDto);

    Offer mapFromOfferResponseToOffer(JobOfferResponse offerResponse);

}

