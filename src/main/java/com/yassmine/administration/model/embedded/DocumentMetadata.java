package com.yassmine.administration.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {
    @Field("_id")
    private String id;
    private String originalName;
    private String url;
    private String mimeType;
    private Long size;
    private LocalDateTime uploadedAt;
    private String fieldName;
}