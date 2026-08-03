package com.yassmine.administration.controller;

import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getByOrderId(orderId), "Détails de la commande"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        org.springframework.data.domain.Page<Order> orderPage = orderService.getAllOrdersPaginated(status, page, limit);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", orderPage.getContent());
        response.put("total", orderPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Order>> createForBooking(
            @PathVariable String bookingId,
            @RequestBody Map<String, Object> payload) {
        Order order = orderService.createForBooking(bookingId, payload);
        return ResponseEntity.ok(ApiResponse.success(order, "Commande créée avec succès"));
    }

    @GetMapping("/booking/{bookingUniqueId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<List<Order>>> getByBooking(@PathVariable String bookingUniqueId) {
        List<Order> orders = orderService.getByBookingId(bookingUniqueId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Commandes récupérées"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'admin', 'ROLE_admin')")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {

        Order updated = orderService.updateStatus(id, payload.get("status"));
        return ResponseEntity.ok(ApiResponse.success(updated, "Statut de la commande mis à jour"));
    }
}