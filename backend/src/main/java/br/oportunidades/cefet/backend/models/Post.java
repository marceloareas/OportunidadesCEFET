package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document("Post")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Post {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String titulo;
    private String corpo;

    // Post 1:1 Usuário
    private String criadorId; // referencia Usuario(id)

    private Date criado;

    @Builder.Default
    // Post 0:N Comentários
    private List<Comentario> idComentarios = new ArrayList<>(); // referencia Comentario(id)

    @Builder.Default
    // Post 1:N Categorias
    private Set<String> idCategorias = new HashSet<>(); // referencia Categoria(id)

    private Date ultimaAtualizacao;

    @Builder.Default
    private Set<String> likesId = new HashSet<>(); // referencia Usuario(id)

}
