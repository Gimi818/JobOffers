package com.joboffers.domain.offer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface OfferRepository  extends MongoRepository<Offer,String> {
    boolean existsByOfferUrl(String offerUrl);

}
