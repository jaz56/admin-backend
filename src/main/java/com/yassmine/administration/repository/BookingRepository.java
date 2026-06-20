package com.yassmine.administration.repository;

import com.yassmine.administration.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    @Query("{ 'user': ?0 }")
    List<Booking> findByUserId(String userId);

    @Query("{ 'user': ?0 }")
    Optional<Booking> findFirstByUserId(String userId);

    @Query("{ 'demande': ?0 }")
    List<Booking> findByDemandeId(String demandeId);

    Optional<Booking> findByUniqueId(String uniqueId);
    Page<Booking> findByStatus(String status, Pageable pageable);

}