package com.yassmine.administration.model.embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class TransactionDetail {
    private String id;
    private Double amount;       // Le montant réel précis
    private String type;             // "credit" ou "debit"
    private String description;      // Ex: "Achat Booking Premium" ou "Cashback reçu"
    private LocalDateTime timestamp;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}