package com.yassmine.administration.controller;

import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.Country;
import com.yassmine.administration.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<java.util.Map<String, Object>> getAllCountries(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int limit) {

        // On appelle le service pour récupérer la page
        org.springframework.data.domain.Page<Country> countryPage = countryService.getAllCountriesPaginated(page, limit);

        // On prépare la réponse EXACTEMENT comme le frontend l'attend
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", countryPage.getContent());
        response.put("total", countryPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<Country>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(countryService.getByCode(code), "Détails du pays"));
    }
}
