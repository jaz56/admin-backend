package com.yassmine.administration.repository;

import com.yassmine.administration.model.Order;
import com.yassmine.administration.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    // Utilisé dans OrderService pour le lookup de paiement
    Optional<Order> findByOrderId(String orderId);
    @Query("{ 'user_id': ?0 }")
    List<Order> findByUserId(String userId);

    // Optionnel : Trouver l'order lié à un booking spécifique
    @Query("{ 'booking_id': ?0 }")
    List<Order> findByBookingId(String bookingId);
    Page<Order> findByStatus(String status, Pageable pageable);

}
