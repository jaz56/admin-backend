package com.yassmine.administration.repository;

import com.yassmine.administration.model.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends MongoRepository<Country, String> {

    // ← @Query explicite car le champ MongoDB est "is_active" (snake_case)
    @Query("{ 'is_active': true }")
    List<Country> findByIsActiveTrue();

    // ← Tri alphabétique dans la query
    @Query("{ 'is_active': true }")
    List<Country> findByIsActiveTrueOrderByNameAsc();

    // ← findByCode sur le champ "code"
    @Query("{ 'code': ?0 }")
    Optional<Country> findByCode(String code);

    // ← Pour la page countries avec pagination
    Page<Country> findAll(Pageable pageable);

    // ← Alias utilisé dans CountryController /active
    @Query("{ 'is_active': true }")
    List<Country> findAllActive();
}