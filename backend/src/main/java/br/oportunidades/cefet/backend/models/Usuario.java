package br.oportunidades.cefet.backend.models;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document("Usuario")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Usuario {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String nome;
    private String email;
    private String senha; // senha criptografada
    private FuncaoDeUsuario funcao;
    private Date criado;

    @Builder.Default
    // Usuário 0:N Posts
    private List<Post> posts = new ArrayList<>(); // referencia Post(id)

    @Builder.Default
    // Usuário 0:N Comentários
    private List<Comentario> comentarios = new ArrayList<>(); // rerefencia Comentario(id)

    @Builder.Default
    private List<String> idCandidaturas = new ArrayList<>(); // referencia Oportunidade(id)

}
