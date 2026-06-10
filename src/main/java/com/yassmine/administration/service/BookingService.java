package com.yassmine.administration.service;

import com.yassmine.administration.exception.ResourceNotFoundException;
import com.yassmine.administration.model.Booking;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    public Booking createForUser(Booking booking, String userEmail) {
        User user = userService.getByEmail(userEmail);

        // S'assurer que userId est bien défini
        booking.setUserId(user.getId());

        if (booking.getUniqueId() == null || booking.getUniqueId().isEmpty()) {
            booking.setUniqueId("BK" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 8).toUpperCase());
        }

        booking.setInterviewStatus("En attente");
        booking.setInterviewCompleted(false);
        booking.setPaymentStatus("paid");
        booking.setStatus("confirmed");
        booking.setStatusUpdateDate("no");

        return bookingRepository.save(booking);
    }

    public List<Booking> findByUserEmail(String userEmail) {
        User user = userService.getByEmail(userEmail);
        return bookingRepository.findByUserId(user.getId());
    }

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

    public Booking getMyBooking(String userId) {
        try {
            List<Booking> bookings = bookingRepository.findByUserId(userId);
            if (bookings != null && !bookings.isEmpty()) {
                return bookings.stream()
                        .filter(b -> !"cancelled".equals(b.getStatus()))
                        .findFirst()
                        .orElse(bookings.get(0));
            }
            return null;
        } catch (Throwable t) {
            t.printStackTrace(); // Si ça crash ici, on le verra enfin dans le terminal !
            return null;
        }
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
}