package com.yassmine.administration;

import com.yassmine.administration.model.ConfigItem;
import com.yassmine.administration.model.Country;
import com.yassmine.administration.model.Role;
import com.yassmine.administration.repository.ConfigItemRepository;
import com.yassmine.administration.repository.CountryRepository;
import com.yassmine.administration.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class AdministrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministrationApplication.class, args);
    }

    @Bean
    CommandLineRunner initRoles(RoleRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Role admin   = new Role(); admin.setValue("admin");     admin.setLabel("Admin");      admin.setSystem(true);
                Role candidat= new Role(); candidat.setValue("candidat"); candidat.setLabel("Candidat"); candidat.setSystem(true);
                Role company = new Role(); company.setValue("company"); company.setLabel("Entreprise"); company.setSystem(true);
                repo.saveAll(List.of(admin, candidat, company));
            }
        };
    }

    @Bean
    CommandLineRunner initConfig(ConfigItemRepository repo) {
        return args -> {
            // ── Fonctions ──────────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("fonction").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "fonction", "Technique",     "Technique",     null, null, 1, true),
                        new ConfigItem(null, "fonction", "Commercial",    "Commercial",    null, null, 2, true),
                        new ConfigItem(null, "fonction", "Administratif", "Administratif", null, null, 3, true),
                        new ConfigItem(null, "fonction", "info",          "Informatique",  null, null, 4, true),
                        new ConfigItem(null, "fonction", "Sante",         "Santé",         null, null, 5, true),
                        new ConfigItem(null, "fonction", "Education",     "Éducation",     null, null, 6, true),
                        new ConfigItem(null, "fonction", "Autre",         "Autre",         null, null, 7, true)
                ));
            }

            // ── Statuts candidat ───────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("statut_candidat").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "statut_candidat", "pending",  "En attente", null, "warning", 1, true),
                        new ConfigItem(null, "statut_candidat", "rejected", "Rejeté",     null, "danger",  2, true),
                        new ConfigItem(null, "statut_candidat", "accepte",  "Éligible",   null, "success", 3, true)
                ));
            }

            // ── Étapes d'inscription ───────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("progress_step").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "progress_step", "STEP_1",           "Étape 1 — Création du compte",             null, null, 1, true),
                        new ConfigItem(null, "progress_step", "STEP_2",           "Étape 2 — Vérification email",              null, null, 2, true),
                        new ConfigItem(null, "progress_step", "STEP_3_SUBSTEP_1", "Étape 3.1 — Informations personnelles",     null, null, 3, true),
                        new ConfigItem(null, "progress_step", "STEP_3_SUBSTEP_2", "Étape 3.2 — Informations professionnelles", null, null, 4, true),
                        new ConfigItem(null, "progress_step", "STEP_3_SUBSTEP_3", "Étape 3.3 — Documents uploadés",            null, null, 5, true),
                        new ConfigItem(null, "progress_step", "STEP_3_SUBSTEP_4", "Étape 3.4 — Échantillon vocal",             null, null, 6, true),
                        new ConfigItem(null, "progress_step", "STEP_4",           "Étape 4 — Profil complété",                 null, null, 7, true)
                ));
            }

            // ── Statuts demande ────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("statut_demande").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "statut_demande", "pre_selection", "Pré-sélection", null, "primary", 1, true),
                        new ConfigItem(null, "statut_demande", "selection",     "Sélection",     null, "primary", 2, true),
                        new ConfigItem(null, "statut_demande", "accepte",       "Accepté",       null, "success", 3, true),
                        new ConfigItem(null, "statut_demande", "refuse",        "Refusé",        null, "danger",  4, true),
                        new ConfigItem(null, "statut_demande", "en_attente",    "En attente",    null, "warning", 5, true)
                ));
            }

            // ── Types de demande ───────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("type_demande").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "type_demande", "travail", "Travail", null, null, 1, true),
                        new ConfigItem(null, "type_demande", "etude",   "Études",  null, null, 2, true),
                        new ConfigItem(null, "type_demande", "stage",   "Stage",   null, null, 3, true)
                ));
            }

            // ── Destinations ───────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("destination_demande").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "destination_demande", "journees_france",    "🇫🇷 France",    null, null, 1, true),
                        new ConfigItem(null, "destination_demande", "journees_canada",    "🇨🇦 Canada",    null, null, 2, true),
                        new ConfigItem(null, "destination_demande", "journees_allemagne", "🇩🇪 Allemagne", null, null, 3, true),
                        new ConfigItem(null, "destination_demande", "journees_belgique",  "🇧🇪 Belgique",  null, null, 4, true)
                ));
            }

            // ── Statuts professionnels ─────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("statut_professionnel").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "statut_professionnel", "employe",   "Employé",     null, null, 1, true),
                        new ConfigItem(null, "statut_professionnel", "chomage",   "Sans emploi", null, null, 2, true),
                        new ConfigItem(null, "statut_professionnel", "etudiant",  "Étudiant",    null, null, 3, true),
                        new ConfigItem(null, "statut_professionnel", "freelance", "Freelance",   null, null, 4, true)
                ));
            }
// ── Types de booking ──────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("type_booking").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "type_booking", "simple",   "Simple",   null, null, 1, true),
                        new ConfigItem(null, "type_booking", "standard", "Standard", null, null, 2, true),
                        new ConfigItem(null, "type_booking", "premium",  "Premium",  null, null, 3, true),
                        new ConfigItem(null, "type_booking", "pro",      "Pro",      null, null, 4, true)
                ));
            }

// ── Statuts paiement ───────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("statut_paiement").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "statut_paiement", "paid",    "Payé",       null, "success", 1, true),
                        new ConfigItem(null, "statut_paiement", "pending", "En attente", null, "warning", 2, true),
                        new ConfigItem(null, "statut_paiement", "created", "Créé",       null, "primary", 3, true),
                        new ConfigItem(null, "statut_paiement", "failed",  "Échoué",     null, "danger",  4, true)
                ));
            }

// ── Statuts entretien ──────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("statut_entretien").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "statut_entretien", "En attente",              "En attente",              null, "warning", 1, true),
                        new ConfigItem(null, "statut_entretien", "En cours",                "En cours",                null, "primary", 2, true),
                        new ConfigItem(null, "statut_entretien", "En attente des résultats","En attente des résultats",null, "warning", 3, true),
                        new ConfigItem(null, "statut_entretien", "Complété",                "Complété",                null, "success", 4, true),
                        new ConfigItem(null, "statut_entretien", "Admis",                   "Admis",                   null, "success", 5, true),
                        new ConfigItem(null, "statut_entretien", "Dossier Finalisé",        "Dossier Finalisé",        null, "success", 6, true),
                        new ConfigItem(null, "statut_entretien", "Annulé",                  "Annulé",                  null, "danger",  7, true)
                ));
            }

// ── Statuts entretien complété ─────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("entretien_complete").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "entretien_complete", "yes", "Complété",     null, null, 1, true),
                        new ConfigItem(null, "entretien_complete", "no",  "Non complété", null, null, 2, true)
                ));
            }
            // ── Devises ────────────────────────────────────────────────────────
            if (repo.findByCategoryAndActiveTrueOrderByOrderAsc("devise").isEmpty()) {
                repo.saveAll(List.of(
                        new ConfigItem(null, "devise", "TND", "TND — Dinar tunisien", null, null, 1, true),
                        new ConfigItem(null, "devise", "EUR", "EUR — Euro",           null, null, 2, true),
                        new ConfigItem(null, "devise", "USD", "USD — Dollar américain",null, null, 3, true),
                        new ConfigItem(null, "devise", "CAD", "CAD — Dollar canadien", null, null, 4, true),
                        new ConfigItem(null, "devise", "GBP", "GBP — Livre sterling",  null, null, 5, true)
                ));
            }
            System.out.println("✅ Config initialisée.");
        };
    }
    @Bean
    CommandLineRunner initCountries(CountryRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            List<Country> pays = List.of(
                    country(1,   "AF", "Afghanistan",                      "Asie"),
                    country(2,   "AL", "Albanie",                          "Europe"),
                    country(3,   "DZ", "Algérie",                          "Afrique"),
                    country(4,   "AD", "Andorre",                          "Europe"),
                    country(5,   "AO", "Angola",                           "Afrique"),
                    country(6,   "AG", "Antigua-et-Barbuda",               "Amérique"),
                    country(7,   "AR", "Argentine",                        "Amérique"),
                    country(8,   "AM", "Arménie",                          "Asie"),
                    country(9,   "AU", "Australie",                        "Océanie"),
                    country(10,  "AT", "Autriche",                         "Europe"),
                    country(11,  "AZ", "Azerbaïdjan",                      "Asie"),
                    country(12,  "BS", "Bahamas",                          "Amérique"),
                    country(13,  "BH", "Bahreïn",                          "Moyen-Orient"),
                    country(14,  "BD", "Bangladesh",                       "Asie"),
                    country(15,  "BB", "Barbade",                          "Amérique"),
                    country(16,  "BY", "Biélorussie",                      "Europe"),
                    country(17,  "BE", "Belgique",                         "Europe"),
                    country(18,  "BZ", "Belize",                           "Amérique"),
                    country(19,  "BJ", "Bénin",                            "Afrique"),
                    country(20,  "BT", "Bhoutan",                          "Asie"),
                    country(21,  "BO", "Bolivie",                          "Amérique"),
                    country(22,  "BA", "Bosnie-Herzégovine",               "Europe"),
                    country(23,  "BW", "Botswana",                         "Afrique"),
                    country(24,  "BR", "Brésil",                           "Amérique"),
                    country(25,  "BN", "Brunéi",                           "Asie"),
                    country(26,  "BG", "Bulgarie",                         "Europe"),
                    country(27,  "BF", "Burkina Faso",                     "Afrique"),
                    country(28,  "BI", "Burundi",                          "Afrique"),
                    country(29,  "CV", "Cap-Vert",                         "Afrique"),
                    country(30,  "KH", "Cambodge",                         "Asie"),
                    country(31,  "CM", "Cameroun",                         "Afrique"),
                    country(32,  "CA", "Canada",                           "Amérique du Nord"),
                    country(33,  "CF", "République centrafricaine",        "Afrique"),
                    country(34,  "TD", "Tchad",                            "Afrique"),
                    country(35,  "CL", "Chili",                            "Amérique"),
                    country(36,  "CN", "Chine",                            "Asie"),
                    country(37,  "CO", "Colombie",                         "Amérique"),
                    country(38,  "KM", "Comores",                          "Afrique"),
                    country(39,  "CG", "Congo",                            "Afrique"),
                    country(40,  "CD", "Congo (RDC)",                      "Afrique"),
                    country(41,  "CR", "Costa Rica",                       "Amérique"),
                    country(42,  "HR", "Croatie",                          "Europe"),
                    country(43,  "CU", "Cuba",                             "Amérique"),
                    country(44,  "CY", "Chypre",                           "Europe"),
                    country(45,  "CZ", "République tchèque",               "Europe"),
                    country(46,  "DK", "Danemark",                         "Europe"),
                    country(47,  "DJ", "Djibouti",                         "Afrique"),
                    country(48,  "DM", "Dominique",                        "Amérique"),
                    country(49,  "DO", "République dominicaine",           "Amérique"),
                    country(50,  "EC", "Équateur",                         "Amérique"),
                    country(51,  "EG", "Égypte",                           "Afrique"),
                    country(52,  "SV", "Salvador",                         "Amérique"),
                    country(53,  "GQ", "Guinée équatoriale",               "Afrique"),
                    country(54,  "ER", "Érythrée",                         "Afrique"),
                    country(55,  "EE", "Estonie",                          "Europe"),
                    country(56,  "SZ", "Eswatini",                         "Afrique"),
                    country(57,  "ET", "Éthiopie",                         "Afrique"),
                    country(58,  "FJ", "Fidji",                            "Océanie"),
                    country(59,  "FI", "Finlande",                         "Europe"),
                    country(60,  "FR", "France",                           "Europe"),
                    country(61,  "GA", "Gabon",                            "Afrique"),
                    country(62,  "GM", "Gambie",                           "Afrique"),
                    country(63,  "GE", "Géorgie",                          "Asie"),
                    country(64,  "DE", "Allemagne",                        "Europe"),
                    country(65,  "GH", "Ghana",                            "Afrique"),
                    country(66,  "GR", "Grèce",                            "Europe"),
                    country(67,  "GD", "Grenade",                          "Amérique"),
                    country(68,  "GT", "Guatemala",                        "Amérique"),
                    country(69,  "GN", "Guinée",                           "Afrique"),
                    country(70,  "GW", "Guinée-Bissau",                    "Afrique"),
                    country(71,  "GY", "Guyana",                           "Amérique"),
                    country(72,  "HT", "Haïti",                            "Amérique"),
                    country(73,  "HN", "Honduras",                         "Amérique"),
                    country(74,  "HU", "Hongrie",                          "Europe"),
                    country(75,  "IS", "Islande",                          "Europe"),
                    country(76,  "IN", "Inde",                             "Asie"),
                    country(77,  "ID", "Indonésie",                        "Asie"),
                    country(78,  "IR", "Iran",                             "Moyen-Orient"),
                    country(79,  "IQ", "Irak",                             "Moyen-Orient"),
                    country(80,  "IE", "Irlande",                          "Europe"),
                    country(81,  "IL", "Israël",                           "Moyen-Orient"),
                    country(82,  "IT", "Italie",                           "Europe"),
                    country(83,  "JM", "Jamaïque",                         "Amérique"),
                    country(84,  "JP", "Japon",                            "Asie"),
                    country(85,  "JO", "Jordanie",                         "Moyen-Orient"),
                    country(86,  "KZ", "Kazakhstan",                       "Asie"),
                    country(87,  "KE", "Kenya",                            "Afrique"),
                    country(88,  "KI", "Kiribati",                         "Océanie"),
                    country(89,  "KP", "Corée du Nord",                    "Asie"),
                    country(90,  "KR", "Corée du Sud",                     "Asie"),
                    country(91,  "KW", "Koweït",                           "Moyen-Orient"),
                    country(92,  "KG", "Kirghizistan",                     "Asie"),
                    country(93,  "LA", "Laos",                             "Asie"),
                    country(94,  "LV", "Lettonie",                         "Europe"),
                    country(95,  "LB", "Liban",                            "Moyen-Orient"),
                    country(96,  "LS", "Lesotho",                          "Afrique"),
                    country(97,  "LR", "Libéria",                          "Afrique"),
                    country(98,  "LY", "Libye",                            "Afrique"),
                    country(99,  "LI", "Liechtenstein",                    "Europe"),
                    country(100, "LT", "Lituanie",                         "Europe"),
                    country(101, "LU", "Luxembourg",                       "Europe"),
                    country(102, "MG", "Madagascar",                       "Afrique"),
                    country(103, "MW", "Malawi",                           "Afrique"),
                    country(104, "MY", "Malaisie",                         "Asie"),
                    country(105, "MV", "Maldives",                         "Asie"),
                    country(106, "ML", "Mali",                             "Afrique"),
                    country(107, "MT", "Malte",                            "Europe"),
                    country(108, "MH", "Îles Marshall",                    "Océanie"),
                    country(109, "MR", "Mauritanie",                       "Afrique"),
                    country(110, "MU", "Maurice",                          "Afrique"),
                    country(111, "MX", "Mexique",                          "Amérique"),
                    country(112, "FM", "Micronésie",                       "Océanie"),
                    country(113, "MD", "Moldavie",                         "Europe"),
                    country(114, "MC", "Monaco",                           "Europe"),
                    country(115, "MN", "Mongolie",                         "Asie"),
                    country(116, "ME", "Monténégro",                       "Europe"),
                    country(117, "MA", "Maroc",                            "Afrique"),
                    country(118, "MZ", "Mozambique",                       "Afrique"),
                    country(119, "MM", "Myanmar",                          "Asie"),
                    country(120, "NA", "Namibie",                          "Afrique"),
                    country(121, "NR", "Nauru",                            "Océanie"),
                    country(122, "NP", "Népal",                            "Asie"),
                    country(123, "NL", "Pays-Bas",                         "Europe"),
                    country(124, "NZ", "Nouvelle-Zélande",                 "Océanie"),
                    country(125, "NI", "Nicaragua",                        "Amérique"),
                    country(126, "NE", "Niger",                            "Afrique"),
                    country(127, "NG", "Nigeria",                          "Afrique"),
                    country(128, "MK", "Macédoine du Nord",                "Europe"),
                    country(129, "NO", "Norvège",                          "Europe"),
                    country(130, "OM", "Oman",                             "Moyen-Orient"),
                    country(131, "PK", "Pakistan",                         "Asie"),
                    country(132, "PW", "Palaos",                           "Océanie"),
                    country(133, "PA", "Panama",                           "Amérique"),
                    country(134, "PG", "Papouasie-Nouvelle-Guinée",        "Océanie"),
                    country(135, "PY", "Paraguay",                         "Amérique"),
                    country(136, "PE", "Pérou",                            "Amérique"),
                    country(137, "PH", "Philippines",                      "Asie"),
                    country(138, "PL", "Pologne",                          "Europe"),
                    country(139, "PT", "Portugal",                         "Europe"),
                    country(140, "QA", "Qatar",                            "Moyen-Orient"),
                    country(141, "RO", "Roumanie",                         "Europe"),
                    country(142, "RU", "Russie",                           "Europe/Asie"),
                    country(143, "RW", "Rwanda",                           "Afrique"),
                    country(144, "KN", "Saint-Kitts-et-Nevis",             "Amérique"),
                    country(145, "LC", "Sainte-Lucie",                     "Amérique"),
                    country(146, "VC", "Saint-Vincent-et-les-Grenadines",  "Amérique"),
                    country(147, "WS", "Samoa",                            "Océanie"),
                    country(148, "SM", "Saint-Marin",                      "Europe"),
                    country(149, "ST", "Sao Tomé-et-Principe",             "Afrique"),
                    country(150, "SA", "Arabie Saoudite",                  "Moyen-Orient"),
                    country(151, "SN", "Sénégal",                          "Afrique"),
                    country(152, "RS", "Serbie",                           "Europe"),
                    country(153, "SC", "Seychelles",                       "Afrique"),
                    country(154, "SL", "Sierra Leone",                     "Afrique"),
                    country(155, "SG", "Singapour",                        "Asie"),
                    country(156, "SK", "Slovaquie",                        "Europe"),
                    country(157, "SI", "Slovénie",                         "Europe"),
                    country(158, "SB", "Îles Salomon",                     "Océanie"),
                    country(159, "SO", "Somalie",                          "Afrique"),
                    country(160, "ZA", "Afrique du Sud",                   "Afrique"),
                    country(161, "SS", "Soudan du Sud",                    "Afrique"),
                    country(162, "ES", "Espagne",                          "Europe"),
                    country(163, "LK", "Sri Lanka",                        "Asie"),
                    country(164, "SD", "Soudan",                           "Afrique"),
                    country(165, "SR", "Suriname",                         "Amérique"),
                    country(166, "SE", "Suède",                            "Europe"),
                    country(167, "CH", "Suisse",                           "Europe"),
                    country(168, "SY", "Syrie",                            "Moyen-Orient"),
                    country(169, "TW", "Taïwan",                           "Asie"),
                    country(170, "TJ", "Tadjikistan",                      "Asie"),
                    country(171, "TZ", "Tanzanie",                         "Afrique"),
                    country(172, "TH", "Thaïlande",                        "Asie"),
                    country(173, "TL", "Timor oriental",                   "Asie"),
                    country(174, "TG", "Togo",                             "Afrique"),
                    country(175, "TO", "Tonga",                            "Océanie"),
                    country(176, "TT", "Trinité-et-Tobago",                "Amérique"),
                    country(177, "TN", "Tunisie",                          "Afrique"),
                    country(178, "TR", "Turquie",                          "Europe/Asie"),
                    country(179, "TM", "Turkménistan",                     "Asie"),
                    country(180, "TV", "Tuvalu",                           "Océanie"),
                    country(181, "UG", "Ouganda",                          "Afrique"),
                    country(182, "UA", "Ukraine",                          "Europe"),
                    country(183, "AE", "Émirats Arabes Unis",              "Moyen-Orient"),
                    country(184, "GB", "Royaume-Uni",                      "Europe"),
                    country(185, "US", "États-Unis",                       "Amérique du Nord"),
                    country(186, "UY", "Uruguay",                          "Amérique"),
                    country(187, "UZ", "Ouzbékistan",                      "Asie"),
                    country(188, "VU", "Vanuatu",                          "Océanie"),
                    country(189, "VE", "Venezuela",                        "Amérique"),
                    country(190, "VN", "Vietnam",                          "Asie"),
                    country(191, "YE", "Yémen",                            "Moyen-Orient"),
                    country(192, "ZM", "Zambie",                           "Afrique"),
                    country(193, "ZW", "Zimbabwe",                         "Afrique"),
                    country(194, "PS", "Palestine",                        "Moyen-Orient"),
                    country(195, "XK", "Kosovo",                           "Europe"),
                    country(196, "CI", "Côte d'Ivoire",                    "Afrique"),
                    country(197, "MO", "Macao",                            "Asie"),
                    country(198, "HK", "Hong Kong",                        "Asie")
            );

            repo.saveAll(pays);
            System.out.println("✅ " + pays.size() + " pays initialisés.");
        };
    }

    private Country country(int id, String code, String name, String region) {
        Country c = new Country();
        c.setCountryId(id);
        c.setCode(code);
        c.setName(name);
        c.setRegion(new Country.Region(1, region));
        c.setActive(true);
        c.setJobCount(0);
        c.setCreatedAt(java.time.LocalDateTime.now());
        c.setUpdatedAt(java.time.LocalDateTime.now());
        return c;
    }
}