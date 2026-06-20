package com.yassmine.administration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "roles")
public class Role {
    @Id
    @JsonProperty("id")
    private String id;
    private String value; // Ex: "admin", "recruteur"
    private String label; // Ex: "Administrateur", "Recruteur"
    private boolean system = false;
    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}