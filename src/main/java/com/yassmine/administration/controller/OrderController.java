package com.yassmine.administration.controller;

import com.yassmine.administration.dto.response.ApiResponse;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getAllOrders(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int limit) {

        // On appelle le service pour récupérer la page
        org.springframework.data.domain.Page<Order> orderPage = orderService.getAllOrdersPaginated(status, page, limit);

        // On structure la réponse pour Angular (res.data et res.total)
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", orderPage.getContent());
        response.put("total", orderPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/all")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        //  ICI : On utilise bien getOrdersByUserEmail()
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByUserEmail(userDetails.getUsername()), "Historique d'achats"));
    }
}
