package br.oportunidades.cefet.backend.models;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*;

@Document("Oportunidade")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Oportunidade {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String professorId; // referência ao usuário (professor)
    private String nome;
    private int quantidadeDeVagas;
    private int vagasPreenchidas;
    private Date criado;

    private String idCategoria; // ✅ agora só uma categoria por oportunidade

    @Builder.Default
    private Set<String> idMembros = new HashSet<>();

    @Builder.Default
    private Set<String> idImagens = new HashSet<>();

    @Builder.Default
    private Set<String> idLikes = new HashSet<>();

    @Builder.Default
    private List<String> idCandidatos = new ArrayList<>();
}
