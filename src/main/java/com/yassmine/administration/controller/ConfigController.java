package com.yassmine.administration.controller;

import com.yassmine.administration.model.ConfigItem;
import com.yassmine.administration.repository.ConfigItemRepository;
import com.yassmine.administration.repository.CountryRepository;
import com.yassmine.administration.model.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigItemRepository configItemRepository;
    private final CountryRepository countryRepository;

    // ── Lire une catégorie ─────────────────────────────────────────────
    @GetMapping("/{category}")
    public ResponseEntity<List<ConfigItem>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(
                configItemRepository.findByCategoryAndActiveTrueOrderByOrderAsc(category)
        );
    }



    // ── Créer un item ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ConfigItem> create(@RequestBody ConfigItem item) {
        // Calculer le prochain ordre
        List<ConfigItem> existing = configItemRepository
                .findByCategoryAndActiveTrueOrderByOrderAsc(item.getCategory());
        item.setOrder(existing.size() + 1);
        item.setActive(true);
        return ResponseEntity.ok(configItemRepository.save(item));
    }

    // ── Supprimer (soft delete) ────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        configItemRepository.findById(id).ifPresent(item -> {
            item.setActive(false);
            configItemRepository.save(item);
        });
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ConfigItem> update(@PathVariable String id,
                                             @RequestBody ConfigItem item) {
        return configItemRepository.findById(id).map(existing -> {
            existing.setLabel(item.getLabel());
            existing.setValue(item.getValue());
            existing.setColor(item.getColor());
            return ResponseEntity.ok(configItemRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
}