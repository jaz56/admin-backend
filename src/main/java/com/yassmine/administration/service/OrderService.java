package com.yassmine.administration.service;

import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Order;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.BookingRepository;
import com.yassmine.administration.repository.OrderRepository;
import com.yassmine.administration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final MongoTemplate mongoTemplate;

    public Order getByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'orderId : " + orderId));
    }

    public Page<Order> getAllOrdersPaginated(String status, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        if (status != null && !status.trim().isEmpty()) {
            return orderRepository.findByStatus(status, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    /**
     * Recherche globale dynamique multi-critères : Commandes, Utilisateurs et Bookings.
     */
    public Page<Order> getAllOrdersFiltered(Map<String, String> params, int page, int limit) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // 1. FILTRES UTILISATEUR
        if (hasUserParams(params)) {
            Query userQuery = new Query();
            if (hasText(params, "userRole")) userQuery.addCriteria(Criteria.where("role").is(params.get("userRole")));
            if (hasText(params, "userSexe")) userQuery.addCriteria(Criteria.where("sexe").is(params.get("userSexe")));
            if (hasText(params, "userPays")) userQuery.addCriteria(Criteria.where("pays").regex(params.get("userPays"), "i"));
            if (hasText(params, "userPhone")) userQuery.addCriteria(Criteria.where("phone").regex(params.get("userPhone"), "i"));
            if (hasText(params, "userNationalite")) userQuery.addCriteria(Criteria.where("nationalite").regex(params.get("userNationalite"), "i"));
            if (hasText(params, "userFonction")) userQuery.addCriteria(Criteria.where("fonction").regex(params.get("userFonction"), "i"));
            if (hasText(params, "userCodePostal")) userQuery.addCriteria(Criteria.where("codePostal").is(params.get("userCodePostal")));
            if (hasText(params, "userJobFavori")) userQuery.addCriteria(Criteria.where("jobFavori").regex(params.get("userJobFavori"), "i"));

            List<String> matchedUserIds = mongoTemplate.find(userQuery, User.class)
                    .stream().map(User::getId).collect(Collectors.toList());

            if (matchedUserIds.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, limit), 0);
            }
            criteriaList.add(Criteria.where("userId").in(matchedUserIds));
        }

        // 2. FILTRES ENTRETIEN / BOOKING
        if (hasBookingParams(params)) {
            Query bookingQuery = new Query();
            if (hasText(params, "bookingDestination")) bookingQuery.addCriteria(Criteria.where("journeesDestination").is(params.get("bookingDestination")));
            if (hasText(params, "appointmentType")) bookingQuery.addCriteria(Criteria.where("appointmentType").is(params.get("appointmentType")));
            if (hasText(params, "paymentStatus")) bookingQuery.addCriteria(Criteria.where("paymentStatus").is(params.get("paymentStatus")));
            if (hasText(params, "interviewStatus")) bookingQuery.addCriteria(Criteria.where("interviewStatus").is(params.get("interviewStatus")));
            if (hasText(params, "interviewCompleted")) bookingQuery.addCriteria(Criteria.where("interviewCompleted").is(Boolean.parseBoolean(params.get("interviewCompleted"))));
            if (hasText(params, "appointmentTime")) bookingQuery.addCriteria(Criteria.where("appointmentTime").is(params.get("appointmentTime")));
            if (hasText(params, "appointmentYear")) bookingQuery.addCriteria(Criteria.where("appointmentYear").is(Integer.parseInt(params.get("appointmentYear"))));

            List<String> matchedBookingIds = mongoTemplate.find(bookingQuery, Booking.class)
                    .stream().map(Booking::getUniqueId).collect(Collectors.toList());

            if (matchedBookingIds.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, limit), 0);
            }
            criteriaList.add(Criteria.where("bookingId").in(matchedBookingIds));
        }

        // 3. FILTRES DIRECTS COMMANDE (ORDER)
        if (hasText(params, "search")) {
            String search = params.get("search");
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("orderId").regex(search, "i"),
                    Criteria.where("bookingId").regex(search, "i"),
                    Criteria.where("userId").regex(search, "i"),
                    Criteria.where("description").regex(search, "i")
            );
            criteriaList.add(searchCriteria);
        }

        if (hasText(params, "type")) criteriaList.add(Criteria.where("type").is(params.get("type")));
        if (hasText(params, "status")) criteriaList.add(Criteria.where("status").is(params.get("status")));
        if (hasText(params, "currency")) criteriaList.add(Criteria.where("currency").is(params.get("currency")));
        if (hasText(params, "country")) criteriaList.add(Criteria.where("country").regex(params.get("country"), "i"));

        // Intervalle de Prix
        if (hasText(params, "priceMin") || hasText(params, "priceMax")) {
            Criteria priceCriteria = Criteria.where("amount");
            if (hasText(params, "priceMin")) priceCriteria.gte(Double.parseDouble(params.get("priceMin")));
            if (hasText(params, "priceMax")) priceCriteria.lte(Double.parseDouble(params.get("priceMax")));
            criteriaList.add(priceCriteria);
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        long total = mongoTemplate.count(query, Order.class);
        query.with(pageable);

        List<Order> orders = mongoTemplate.find(query, Order.class);
        return new PageImpl<>(orders, pageable, total);
    }

    private boolean hasText(Map<String, String> params, String key) {
        return params.containsKey(key) && StringUtils.hasText(params.get(key));
    }

    private boolean hasUserParams(Map<String, String> p) {
        return hasText(p, "userRole") || hasText(p, "userSexe") || hasText(p, "userPays") ||
                hasText(p, "userPhone") || hasText(p, "userNationalite") || hasText(p, "userFonction") ||
                hasText(p, "userCodePostal") || hasText(p, "userJobFavori");
    }

    private boolean hasBookingParams(Map<String, String> p) {
        return hasText(p, "bookingDestination") || hasText(p, "appointmentType") ||
                hasText(p, "paymentStatus") || hasText(p, "interviewStatus") ||
                hasText(p, "interviewCompleted") || hasText(p, "appointmentTime") ||
                hasText(p, "appointmentYear");
    }

    public Order createForBooking(String bookingId, Map<String, Object> payload) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking introuvable"));

        if (!"confirmed".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Impossible de créer une commande : le booking doit être 'confirmed'");
        }
        if (!"paid".equalsIgnoreCase(booking.getPaymentStatus())) {
            throw new RuntimeException("Impossible de créer une commande : le paiement doit être 'paid'");
        }

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(booking.getUserId());
        order.setBookingId(booking.getUniqueId());
        order.setType(booking.getAppointmentType());
        order.setStatus((String) payload.getOrDefault("status", "created"));
        order.setCurrency((String) payload.getOrDefault("currency", "TND"));
        order.setCountry((String) payload.getOrDefault("country", ""));

        Object amountObj = payload.get("amount");
        if (amountObj instanceof Number) {
            order.setAmount(((Number) amountObj).doubleValue());
        } else if (booking.getPrice() != null) {
            order.setAmount(booking.getPrice());
        }

        Object amountInTndObj = payload.get("amountInTND");
        if (amountInTndObj instanceof Number) {
            order.setAmountInTND(((Number) amountInTndObj).doubleValue());
        }

        if (payload.containsKey("description") && payload.get("description") != null) {
            order.setDescription((String) payload.get("description"));
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
}