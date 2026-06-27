package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document("feed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FeedItem {

    @Id
    private String id;

    private String referenciaId; // id do Post ou Oportunidade
    private String tipo; // POST ou OPORTUNIDADE

    private int likesCount;
    private int comentariosCount;

    @Indexed
    private Date createdAt = new Date();
}