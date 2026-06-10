package com.yassmine.administration.service;

import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Country;
import com.yassmine.administration.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public List<Country> getAllActive() {
        return countryRepository.findByIsActiveTrue();
    }

    public Country getByCode(String code) {
        return countryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Pays introuvable avec le code : " + code));
    }
    public Page<Country> getAllCountriesPaginated(int page, int limit) {
        // Tri alphabétique sur le nom du pays
        Pageable pageable = PageRequest.of(page, limit, Sort.by("name").ascending());
        return countryRepository.findAll(pageable);
    }
}
