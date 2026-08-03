package com.yassmine.administration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    private String date;

    private String time;

    @Field("order_id")
    private String orderId;

    @Field("booking_id")
    private String bookingId;

    private String type;

    private String status;

    // Remplacé "price" par "amount" pour correspondre au JSON
    private Double amount;

    // Nouveaux champs détectés dans le JSON "balance_topup"
    private Double amountInTND;

    private String description;

    private String currency;     // TND

    private String country;

    @Field("user_id")
    private String userId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Optionnel : Capture le champ "__v" généré par Mongoose/Node.js
    @Version
    @Field("__v")
    private Long version;
}