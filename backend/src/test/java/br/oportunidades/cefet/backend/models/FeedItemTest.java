package br.oportunidades.cefet.backend.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FeedItemTest {

    @Test
    void builderAplicaDefaultDeCreatedAt() {
        FeedItem item = FeedItem.builder()
                .referenciaId("abc")
                .tipo("POST")
                .build();

        assertNotNull(item.getCreatedAt());
    }
}
