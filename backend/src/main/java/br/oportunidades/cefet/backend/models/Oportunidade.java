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

    //Oportunidade 1:N Usuario
    private String professorId; // referencia Usuário(id)

    @Builder.Default
    private Set<String> idMembros = new HashSet<>(); // referencia Usuario(id)
                                                     // Outros membros cadastrados para a oportunidade

    private String nome;
    private int quantidadeDeVagas;
    private int vagasPreenchidas;
    private Date criado;

    @Builder.Default
    private Set<Categoria> idCategorias = new HashSet<>(); // referencia Categoria(id)

    @Builder.Default
    private Set<String> idImagens = new HashSet<>(); // referencia Imagem(id)
                                                     // Ids das Imagens adicionadas ao criar uma oportunidade(opcional)

    @Builder.Default
    private Set<String> idLikes = new HashSet<>(); //referencia Usuario(id)

    @Builder.Default
    private List<String> idCandidatos = new ArrayList<>(); //referencia Usuario(id)
}
