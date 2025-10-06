package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
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

    // Cometário 1:1 Usuário
    private String usuarioId; // referencia Usuario(id)

    // Comentário 0:N Comentário
    private String idComentarioPai; // referencia Comentario(id)

    private String tipoEntidadePai; // "Oportunidade" ou "Post"
                                    // Identificar se foi um Comentário feito em um Post ou em uma Oportunidade,
                                    // pois a entidade Comentario é única(tanto para Post quanto para Oportunidade)
                                    // facilitar busca no Banco

    // Comentário 1:1 Post
    private String idPots; // referencia Post(id)

    private Date dataComentario;

    @Builder.Default
    private Set<String> likesId = new HashSet<>(); // referencia Usuario(id)
    
}
