package br.oportunidades.cefet.backend.dto.item_salvo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavedItemDTO {

    private String userId;
    private String feedItemId;

    public String getUserId() {
        return userId;
    }

    public String getFeedItemId() {
        return feedItemId;
    }
}