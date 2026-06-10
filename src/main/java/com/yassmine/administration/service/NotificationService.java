package com.yassmine.administration.service;

import com.yassmine.administration.model.Notification;
import com.yassmine.administration.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Récupère toutes les notifications d'un utilisateur, de la plus récente à la plus ancienne.
     */
    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Marque une notification spécifique comme lue.
     */
    public void markAsRead(String id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues.
     */
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // On filtre pour ne mettre à jour que celles qui ne sont pas encore lues
        List<Notification> unreadNotifications = notifications.stream()
                .filter(n -> !n.isRead())
                .peek(n -> n.setRead(true))
                .toList();

        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    /**
     * Supprime une notification de la base de données.
     */
    public void deleteNotification(String id) {
        notificationRepository.deleteById(id);
    }

    /**
     * 💡 MÉTHODE BONUS : Permet de créer et d'enregistrer facilement une notification.
     * Tu pourras l'injecter dans tes autres services (DemandeService, BookingService...).
     * * @param type "SUCCESS", "INFO", "WARNING", "PURPLE", "TEAL"
     */
    public Notification createNotification(String userId, String type, String title, String message, String route) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type.toUpperCase()) // On force le majuscule pour correspondre à notre UI Angular
                .title(title)
                .message(message)
                .route(route)
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }
}