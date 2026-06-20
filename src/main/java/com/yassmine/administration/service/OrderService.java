package com.yassmine.administration.service;


import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.BookingRepository;
import com.yassmine.administration.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;



    public Order getByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'orderId : " + orderId));
    }


    public Page<Order> getAllOrdersPaginated(String status, int page, int limit) {
        // Tri décroissant sur la date de création (la plus fiable et toujours présente)
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        // Si un filtre par statut est appliqué
        if (status != null && !status.trim().isEmpty()) {
            return orderRepository.findByStatus(status, pageable);
        }

        // Sinon on retourne tout
        return orderRepository.findAll(pageable);
    }

        public Order createForBooking(String bookingId, Map<String, Object> payload) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking introuvable"));

            // ── CONTRAINTE ──
            if (!"confirmed".equalsIgnoreCase(booking.getStatus())) {
                throw new RuntimeException(
                        "Impossible de créer une commande : le booking doit être au statut 'confirmed' (statut actuel : "
                                + booking.getStatus() + ")"
                );
            }
            if (!"paid".equalsIgnoreCase(booking.getPaymentStatus())) {
                throw new RuntimeException(
                        "Impossible de créer une commande : le paiement du booking doit être 'paid' (statut actuel : "
                                + booking.getPaymentStatus() + ")"
                );
            }

            Order order = new Order();
            order.setOrderId(UUID.randomUUID().toString());
            order.setUserId(booking.getUserId());
            order.setBookingId(booking.getUniqueId());
            order.setType(booking.getAppointmentType());
            order.setStatus((String) payload.getOrDefault("status", "created"));
            order.setCurrency((String) payload.getOrDefault("currency", "TND"));
            order.setCountry((String) payload.getOrDefault("country", ""));

            Object priceObj = payload.get("price");
            if (priceObj instanceof Number) {
                order.setPrice(((Number) priceObj).doubleValue());
            } else if (booking.getPrice() != null) {
                order.setPrice(booking.getPrice());
            }

            LocalDate now = LocalDate.now();
            LocalTime time = LocalTime.now();
            order.setDate((String) payload.getOrDefault("date", now.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            order.setTime((String) payload.getOrDefault("time", time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

            return orderRepository.save(order);
        }

        public List<Order> getByBookingId(String bookingUniqueId) {
            return orderRepository.findByBookingId(bookingUniqueId);
        }
    public Order updateStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order introuvable avec l'id : " + id));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void deleteOrder(String id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order introuvable avec l'id : " + id);
        }
        orderRepository.deleteById(id);
    }

}
