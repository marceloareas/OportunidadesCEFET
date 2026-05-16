package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import br.oportunidades.cefet.backend.enums.GrandeAreaConhecimento;
import java.util.*;

import org.springframework.data.annotation.Transient;

@Document(collection = "Oportunidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Oportunidade {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String nome;
    private String descricao;

    @Indexed
    private String professorId;

    @Builder.Default
    private Integer quantidadeDeVagas = 0;

    @Builder.Default
    private Integer vagasPreenchidas = 0;

    private String idCategoria;
    private String imagemBase64;

    @Builder.Default
    private Date criado = new Date();

    @Builder.Default
    private List<String> alunosCandidatosId = new ArrayList<>();

    @Builder.Default
    private List<String> alunosAprovadosId = new ArrayList<>();

    @Builder.Default
    private List<String> idLikes = new ArrayList<>();

    @Builder.Default
    private Boolean finalizada = false;

    @Builder.Default
    private List<GrandeAreaConhecimento> grandesAreas = new ArrayList<>();

    @Transient
    private String nomeCriador;

    @Transient
    private String imagemPerfil;

}
