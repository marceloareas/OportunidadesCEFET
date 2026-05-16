package br.oportunidades.cefet.backend.dto.feed;

import br.oportunidades.cefet.backend.models.FeedItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedPageDTO {
    private List<FeedResponseDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}