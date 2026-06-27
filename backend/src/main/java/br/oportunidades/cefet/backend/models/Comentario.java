package br.oportunidades.cefet.backend.models;

import br.oportunidades.cefet.backend.enums.TipoFeed;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document("Comentario")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Comentario {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String usuarioId;

    private TipoFeed tipoEntidadePai;

    @Indexed
    private String idPost;

    @Builder.Default
    private Date createdAt = new Date();

    private String texto;

    @Builder.Default
    private Set<String> idLikes = new HashSet<>();
}
