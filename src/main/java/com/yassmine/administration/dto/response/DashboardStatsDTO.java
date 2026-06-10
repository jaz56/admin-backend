package com.yassmine.administration.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalDemandes;
    private long totalBookings;
    private double totalRevenue;
    private Map<String, Long> demandesByStatus; // Ex: {"pre_selection": 5, "accepte_pour_entretien": 1}
}