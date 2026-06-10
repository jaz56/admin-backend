package com.yassmine.administration.service;


import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;

    public Order createOrder(Order order, String userEmail) {
        User user = userService.getByEmail(userEmail);
        order.setUserId(user.getId());

        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus("created");
        return orderRepository.save(order);
    }

    public Order getByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'orderId : " + orderId));
    }

    public List<Order> getOrdersByUserEmail(String email) {
        User user = userService.getByEmail(email);

        // CORRECTION : On passe l'identifiant plat (user.getId()) au lieu de l'objet user
        return orderRepository.findByUserId(user.getId());
    }
    public Page<Order> getAllOrdersPaginated(String status, int page, int limit) {
        // Tri décroissant sur l'ID ou la date (selon ce qui existe dans ton modèle)
        Pageable pageable = PageRequest.of(page, limit, Sort.by("orderId").descending());

        // Si un filtre par statut est appliqué
        if (status != null && !status.trim().isEmpty()) {
            return orderRepository.findByStatus(status, pageable);
        }

        // Sinon on retourne tout
        return orderRepository.findAll(pageable);
    }
}
