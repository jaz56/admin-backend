package com.yassmine.administration.config;

import com.yassmine.administration.model.Country;
import com.yassmine.administration.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Définition du dictionnaire Code -> Nationalité (en Français)
        Map<String, String> nationalityMap = new HashMap<>();

        // Maghreb & Afrique du Nord
        nationalityMap.put("TN", "Tunisienne");
        nationalityMap.put("MA", "Marocaine");
        nationalityMap.put("DZ", "Algérienne");
        nationalityMap.put("LY", "Libyenne");
        nationalityMap.put("EG", "Égyptienne");
        nationalityMap.put("MR", "Mauritanienne");

        // Europe
        nationalityMap.put("FR", "Française");
        nationalityMap.put("BE", "Belge");
        nationalityMap.put("CH", "Suisse");
        nationalityMap.put("IT", "Italienne");
        nationalityMap.put("ES", "Espagnole");
        nationalityMap.put("DE", "Allemande");
        nationalityMap.put("GB", "Britannique");
        nationalityMap.put("PT", "Portugaise");
        nationalityMap.put("CA", "Canadienne");

        // Afrique Subsaharienne
        nationalityMap.put("SN", "Sénégalaise");
        nationalityMap.put("CI", "Ivoirienne");
        nationalityMap.put("CM", "Camerounaise");
        nationalityMap.put("ML", "Malienne");
        nationalityMap.put("GN", "Guinéenne");
        nationalityMap.put("TG", "Togolaise");
        nationalityMap.put("BJ", "Béninoise");
        nationalityMap.put("BF", "Burkinabè");
        nationalityMap.put("CD", "Congolaise");

        // Autres
        nationalityMap.put("US", "Américaine");
        nationalityMap.put("TR", "Turque");
        nationalityMap.put("SA", "Saoudienne");
        nationalityMap.put("AE", "Émirienne");

        System.out.println("====== INITIALISATION DES NATIONALITÉS ======");

        // 2. Parcourir le dictionnaire et mettre à jour la base de données
        nationalityMap.forEach((code, nationality) -> {
            // On cherche le pays par son code (s'adapte si vous utilisez des codes à 3 lettres ex: TUN, FRA)
            Optional<Country> countryOpt = countryRepository.findByCode(code);

            if (countryOpt.isPresent()) {
                Country country = countryOpt.get();
                // On n'applique la mise à jour que si le champ est vide
                if (country.getNationality() == null || country.getNationality().isEmpty()) {
                    country.setNationality(nationality);
                    countryRepository.save(country);
                    System.out.println("Mise à jour réussie pour le pays : " + country.getName() + " -> " + nationality);
                }
            } else {
                // Optionnel : Si le pays n'existe pas du tout en BDD, on peut le créer proprement ici
                Country newCountry = new Country();
                newCountry.setCode(code);
                newCountry.setName(getCountryNameDefault(code)); // Nom par défaut
                newCountry.setNationality(nationality);
                newCountry.setActive(true); // On le rend actif par défaut
                countryRepository.save(newCountry);
                System.out.println("Nouveau pays créé : " + newCountry.getName() + " -> " + nationality);
            }
        });

        System.out.println("=============================================");
    }

    // Méthode utilitaire simple pour donner un nom si le pays est créé de zéro
    private String getCountryNameDefault(String code) {
        return switch (code) {
            case "GRID" -> "Tunisie";
            case "TN" -> "Tunisie";
            case "FR" -> "France";
            case "MA" -> "Maroc";
            case "DZ" -> "Algérie";
            case "BE" -> "Belgique";
            case "CH" -> "Suisse";
            case "CA" -> "Canada";
            case "US" -> "États-Unis";
            default -> "Pays (" + code + ")";
        };
    }
}