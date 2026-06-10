package com.yassmine.administration.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {
    private BigDecimal amount;       // Le montant réel précis
    private String type;             // "credit" ou "debit"
    private String description;      // Ex: "Achat Booking Premium" ou "Cashback reçu"
    private LocalDateTime timestamp;
}