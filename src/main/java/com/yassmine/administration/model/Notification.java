package com.yassmine.administration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;
    private String userId; // Lié à l'ID de l'utilisateur connecté
    private String type; // success, info, warning
    private String title;
    private String message;
    private String icon; // check_circle, info, warning, etc.
    private String color; // Ex: bg-green-100 text-green-600
    private String route; // /candidate/demande
    private boolean read;

    @CreatedDate
    private LocalDateTime createdAt;
}