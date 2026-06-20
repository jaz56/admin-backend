package com.yassmine.administration.service;

import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.Demande;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.BookingRepository;
import com.yassmine.administration.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final DemandeRepository demandeRepository; // ajouter au constructeur (RequiredArgsConstructor)
    private final MongoTemplate mongoTemplate;

    public Booking getById(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'id : " + id));
    }

    public Booking updateInterviewScore(String id, BigDecimal score) {
        Booking booking = getById(id);
        booking.setInterviewScore(score);
        booking.setInterviewCompleted(true);
        booking.setInterviewStatus("Complété");
        return bookingRepository.save(booking);
    }

    public Booking reschedule(String id, java.time.LocalDateTime newDate, String newTime) {
        Booking booking = getById(id);
        booking.setAppointmentDate(newDate);
        booking.setAppointmentTime(newTime);
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(String id) {
        Booking booking = getById(id);
        booking.setStatus("cancelled");
        booking.setInterviewStatus("Annulé");
        return bookingRepository.save(booking);
    }

    public void deletePermanent(String id) {
        Booking booking = getById(id);
        bookingRepository.deleteById(booking.getId());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking finaliseInterview(String id, String report, String recordingUrl) {
        Booking booking = getById(id);

        booking.setInterviewReport(report);
        booking.setInterviewRecording(recordingUrl);
        booking.setInterviewStatus("Dossier Finalisé");

        return bookingRepository.save(booking);
    }

    public Page<Booking> getAllBookingsPaginated(String status, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        if (status != null && !status.trim().isEmpty()) {
            return bookingRepository.findByStatus(status, pageable);
        }

        return bookingRepository.findAll(pageable);
    }

    public Booking updateBookingStatus(String id, String status) {
        // 1. On récupère la réservation (gère automatiquement l'erreur si l'id n'existe pas)
        Booking booking = getById(id);

        // 2. On met à jour le statut principal de la réservation ("confirmed", "pending", "cancelled")
        booking.setStatus(status);

        // 3. Cohérence des données : Si on annule ou confirme, on adapte aussi le statut de l'entretien
        if ("cancelled".equalsIgnoreCase(status)) {
            booking.setInterviewStatus("Annulé");
        } else if ("confirmed".equalsIgnoreCase(status) && "Annulé".equals(booking.getInterviewStatus())) {
            booking.setInterviewStatus("En attente");
        }

        // 4. On sauvegarde les modifications dans MongoDB
        return bookingRepository.save(booking);
    }

    public Booking createForDemande(Map<String, Object> payload) {
        String demandeId = (String) payload.get("demande");

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));

        List<String> allowedStatuses = List.of("pre_selection", "acceptee");
        if (!allowedStatuses.contains(demande.getStatus())) {
            throw new RuntimeException(
                    "Impossible de créer un booking : la demande doit être au statut 'Pré-sélection' ou 'Acceptée' (statut actuel : "
                            + demande.getStatus() + ")"
            );
        }

        Booking booking = new Booking();
        booking.setUserId((String) payload.get("user"));
        booking.setDemandeId(demandeId);
        booking.setAppointmentType((String) payload.get("appointmentType"));
        booking.setAppointmentTime((String) payload.get("appointmentTime"));
        booking.setJourneesDestination((String) payload.get("journeesDestination"));

        Object priceObj = payload.get("price");
        if (priceObj instanceof Number) {
            booking.setPrice(((Number) priceObj).doubleValue());
        }

        String dateStr = (String) payload.get("appointmentDate");
        if (dateStr != null && !dateStr.isEmpty()) {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr.substring(0, 10));
            booking.setAppointmentDate(date.atStartOfDay());
        }

        // NOUVEAU : ID séquentiel BK000001, BK000002, ...
        booking.setUniqueId(generateNextBookingId());

        booking.setInterviewStatus("En attente");
        booking.setInterviewCompleted(false);
        booking.setPaymentStatus("pending");
        booking.setStatus("confirmed");
        booking.setStatusUpdateDate("no");

        return bookingRepository.save(booking);
    }    private String generateNextBookingId() {
        Query query = new Query(Criteria.where("uniqueId").regex("^BK\\d+$"));
        query.fields().include("uniqueId");

        List<Booking> bookings = mongoTemplate.find(query, Booking.class);

        int maxId = 0;
        for (Booking b : bookings) {
            String uid = b.getUniqueId();
            if (uid != null && uid.matches("^BK\\d+$")) {
                try {
                    int num = Integer.parseInt(uid.substring(2));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }

        int nextId = maxId + 1;
        return String.format("BK%06d", nextId); // BK000125, etc.
    }
    public Booking updateBookingFields(String id, Map<String, Object> payload) {
        Booking booking = getById(id);

        if (payload.containsKey("appointmentType")) {
            booking.setAppointmentType((String) payload.get("appointmentType"));
        }
        if (payload.containsKey("appointmentTime")) {
            booking.setAppointmentTime((String) payload.get("appointmentTime"));
        }
        if (payload.containsKey("appointmentDate")) {
            String dateStr = (String) payload.get("appointmentDate");
            if (dateStr != null && !dateStr.isEmpty()) {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr.substring(0, 10));
                booking.setAppointmentDate(date.atStartOfDay());
            }
        }
        if (payload.containsKey("price")) {
            Object priceObj = payload.get("price");
            if (priceObj instanceof Number) {
                booking.setPrice(((Number) priceObj).doubleValue());
            }
        }
        if (payload.containsKey("journeesDestination")) {
            booking.setJourneesDestination((String) payload.get("journeesDestination"));
        }
        if (payload.containsKey("paymentStatus")) {
            booking.setPaymentStatus((String) payload.get("paymentStatus"));
        }
        if (payload.containsKey("status")) {
            booking.setStatus((String) payload.get("status"));
        }
        if (payload.containsKey("interviewStatus")) {
            booking.setInterviewStatus((String) payload.get("interviewStatus"));
        }
        if (payload.containsKey("interviewScore")) {
            Object scoreObj = payload.get("interviewScore");
            if (scoreObj instanceof Number) {
                booking.setInterviewScore(java.math.BigDecimal.valueOf(((Number) scoreObj).doubleValue()));
            }
        }
        if (payload.containsKey("interviewReport")) {
            booking.setInterviewReport((String) payload.get("interviewReport"));
        }
        if (payload.containsKey("interviewRecording")) {
            booking.setInterviewRecording((String) payload.get("interviewRecording"));
        }
        if (payload.containsKey("interviewCompleted")) {
            Object completedObj = payload.get("interviewCompleted");
            if (completedObj instanceof Boolean) {
                booking.setInterviewCompleted((Boolean) completedObj);
            }
        }
        if (payload.containsKey("statusUpdateDate")) {
            booking.setStatusUpdateDate((String) payload.get("statusUpdateDate"));
        }
        return bookingRepository.save(booking);
    }
}