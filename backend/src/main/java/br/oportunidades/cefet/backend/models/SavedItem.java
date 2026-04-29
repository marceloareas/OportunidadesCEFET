package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("saved_items")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

public class SavedItem {

    @Id
    private String id;

    private String userId;

    private String feedItemId;

    private LocalDateTime createdAt;

    public SavedItem() {}

    public SavedItem(String userId, String feedItemId) {
        this.userId = userId;
        this.feedItemId = feedItemId;
        this.createdAt = LocalDateTime.now();
    }

}