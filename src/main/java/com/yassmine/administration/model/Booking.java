package com.yassmine.administration.model;

import com.yassmine.administration.model.embedded.DocumentMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;

    @Field("user")
    private String userId;

    // Mapping direct avec l'ID plat du JSON
    @Field("demande")
    private String demandeId;

    private String uniqueId;
    private String appointmentType;   // "premium", "standard"
    private LocalDateTime appointmentDate;
    private String appointmentTime;
    private Double price;
    private String paymentStatus;     // "paid", "pending"
    private String status;            // "confirmed", "cancelled"
    private String journeesDestination;

    // Interview
    private Boolean interviewCompleted;
    private java.math.BigDecimal interviewScore;    private String interviewRecording;
    private String interviewReport;
    private String interviewStatus;   // "En attente", ...
    private String statusUpdateDate;

    @JsonIgnore
    private Map<String, Object> documents = new HashMap<>();
    @JsonIgnore
    private Map<String, Object> documentsMetadata = new HashMap<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
