package com.yassmine.administration.repository;


import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends MongoRepository<Demande, String> {
    // Recherche toutes les demandes associées à un objet User spécifique
    List<Demande> findByUserId(String userId);

    // Optionnel : Recherche par l'identifiant unique généré (ex: 25-08-T-4A8202F772184)
    Optional<Demande> findByUniqueId(String uniqueId);
    Page<Demande> findByStatus(String status, Pageable pageable);
}
