package com.yassmine.administration.controller;

import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.Country;
import com.yassmine.administration.repository.CountryRepository;
import com.yassmine.administration.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;
    private final CountryRepository countryRepository; // ← ajouter

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCountries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        var countryPage = countryService.getAllCountriesPaginated(page, limit);
        Map<String, Object> response = new HashMap<>();
        response.put("data", countryPage.getContent());
        response.put("total", countryPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ── Nouveau : tous les pays actifs pour les selects ───────────────
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, String>>> getActiveCountries() {
        List<Country> countries = countryRepository.findAllActive();

        // Log pour vérifier
        System.out.println("Pays actifs trouvés : " + countries.size());
        countries.forEach(c -> System.out.println("  → " + c.getCode() + " / " + c.getName()));

        List<Map<String, String>> result = countries.stream()
                .filter(c -> c.getName() != null && c.getCode() != null)
                .sorted(Comparator.comparing(Country::getName))
                .map(c -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("value", c.getCode());
                    m.put("label", c.getName());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<Country>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(countryService.getByCode(code), "Détails du pays"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Country>> toggleActive(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        boolean isActive = body.get("isActive");
        Country updated = countryService.toggleActive(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(updated, "Statut mis à jour"));
    }
}