package com.yassmine.administration.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StbInfo {
    private List<String> stbSkills = new ArrayList<>();
    private List<String> stbLanguages = new ArrayList<>();
    private List<String> stbInterests = new ArrayList<>();
    private boolean interviewCompleted;
    private boolean stbProfileCompleted;
    private String stbVerificationStatus; // "pending", "verified", "rejected"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}