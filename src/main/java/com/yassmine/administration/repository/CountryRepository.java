package com.yassmine.administration.repository;


import com.yassmine.administration.model.Country;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends MongoRepository<Country, String> {
    // Utilisé pour charger uniquement les pays disponibles dans les formulaires d'inscription
    List<Country> findByIsActiveTrue();

    // Recherche par code ISO (ex: "AF", "TN", "FR")
    Optional<Country> findByCode(String code);
}
