package com.yassmine.administration.repository;

import com.yassmine.administration.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUniqueId(String uniqueId);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByCandidateVerificationStatus(String status);
    Page<User> findByRole(String role, Pageable pageable);
}
