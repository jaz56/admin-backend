package com.yassmine.administration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDetail {
    private boolean read;   // Consulter
    private boolean create; // Ajouter
    private boolean update; // Modifier
}