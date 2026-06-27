package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Transient;

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

    @Indexed
    private String criadorId;

    private Date criado;

    @Builder.Default
    // Post 1:N Categorias
    private Set<String> idCategorias = new HashSet<>(); // referencia Categoria(id)

    private Date ultimaAtualizacao;

    private String imagemBase64;
    
    @Builder.Default
    private List<String> idLikes = new ArrayList<>();

    @Transient
    private String nomeCriador;

    @Transient
    private String imagemPerfil;

}
