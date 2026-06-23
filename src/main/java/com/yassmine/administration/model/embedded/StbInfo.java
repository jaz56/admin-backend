package com.yassmine.administration.model.embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StbInfo {
    private List<String> stbSkills = new ArrayList<>();
    private List<String> stbLanguages = new ArrayList<>();
    private List<String> stbInterests = new ArrayList<>();
    private boolean interviewCompleted;
    private boolean stbProfileCompleted;
    private String stbVerificationStatus; // "pending", "verified", "rejected"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String interviewRecording;
    private String interviewReport;
    private Double interviewScore;
}