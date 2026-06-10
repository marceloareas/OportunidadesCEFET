package br.oportunidades.cefet.backend.models;

import br.oportunidades.cefet.backend.enums.GrandeAreaConhecimento;
import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import br.oportunidades.cefet.backend.enums.StatusOportunidade;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

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

    private Date dataInicioInscricao;
    private Date dataFimInscricao;
    
    @Builder.Default
    private List<String> idLikes = new ArrayList<>();

    @Builder.Default
    private Boolean finalizada = false;

    @Transient
    private StatusOportunidade status;

    @Builder.Default
    private List<GrandeAreaConhecimento> grandesAreas = new ArrayList<>();

    @Transient
    private String nomeCriador;

    @Transient
    private String imagemPerfil;

    // Derivados da coleção Candidatura (não persistidos).
    @Transient
    private List<String> alunosCandidatosId;

    @Transient
    private List<String> alunosAprovadosId;

    // Status da candidatura do aluno solicitante (preenchido em listagens por aluno).
    @Transient
    private StatusCandidatura statusCandidaturaAluno;

}
