package com.yassmine.administration.dto;

import lombok.Data;

@Data
public class OrderFilterDTO {
    // Filtres Commande
    private String search;
    private String type;
    private String status;
    private String currency;
    private Double priceMin;
    private Double priceMax;
    private String dateFrom;
    private String dateTo;

    // Filtres Utilisateur
    private String userRole;
    private String userSexe;
    private String userPays;
    private String userPhone;
    private String userNationalite;
    private String userFonction;
    private String userCodePostal;
    private String userJobFavori;

    // Filtres Booking / Entretien
    private String bookingDestination;
    private String appointmentType;
    private String paymentStatus;
    private String interviewStatus;
    private Boolean interviewCompleted;
    private String appointmentTime;
    private String exactDate;
    private Integer appointmentYear;
}