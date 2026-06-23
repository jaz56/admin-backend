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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class AdministrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministrationApplication.class, args);
    }
    @Bean
    CommandLineRunner initConfig(
            MongoTemplate mongoTemplate,
            RoleRepository roleRepository,
            ConfigItemRepository configItemRepository,
            CountryRepository countryRepository
    ) {
        return args -> {
            // ── Types d'abonnement ─────────────────────────────────────────────
            seedCategory(mongoTemplate, "type_abonnement", List.of(
                    item("basic",   "Basic",   null, 1),
                    item("premium", "Premium", null, 2),
                    item("pro",     "Pro",     null, 3)
            ));

// ── Types de transaction ───────────────────────────────────────────
            seedCategory(mongoTemplate, "type_transaction", List.of(
                    item("debit",  "Débit",  null, 1),
                    item("credit", "Crédit", null, 2)
            ));

// ── Statuts de transaction ─────────────────────────────────────────
            seedCategory(mongoTemplate, "statut_transaction", List.of(
                    item("completed", "Complété",   "success", 1),
                    item("pending",   "En attente", "warning", 2),
                    item("failed",    "Échoué",     "danger",  3)
            ));
            seedCategory(mongoTemplate, "statut_stb", List.of(
                    item("pending",  "En attente", "warning", 1),
                    item("verified", "Vérifié",    "success", 2),
                    item("rejected", "Rejeté",     "danger",  3)
            ));
        };
    }
    private void seedCategory(MongoTemplate mongo, String category, List<ConfigItem> items) {
        long count = mongo.count(
                new Query(Criteria.where("category").is(category)),
                ConfigItem.class
        );
        if (count == 0) {
            items.forEach(i -> i.setCategory(category));
            mongo.insertAll(items);
            System.out.println("✅ Seeded category: " + category + " (" + items.size() + " items)");
        }
    }
    private ConfigItem item(String value, String label, String color, int order) {
        ConfigItem ci = new ConfigItem();
        ci.setValue(value);
        ci.setLabel(label);
        ci.setColor(color);
        ci.setActive(true);
        ci.setOrder(order);
        return ci;
    }
}

