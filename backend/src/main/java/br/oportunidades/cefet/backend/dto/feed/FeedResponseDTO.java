package br.oportunidades.cefet.backend.dto.feed;

import br.oportunidades.cefet.backend.enums.StatusOportunidade;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class FeedResponseDTO {

    // ids
    private String id;
    private String referenciaId;

    // tipo
    private String tipo;

    // criador
    private String criadorId;
    private String nomeCriador;
    private String imagemPerfil;

    // datas
    private Date createdAt;
    private Date dataInicioInscricao;
    private Date dataFimInscricao;

    // conteúdo
    private String titulo;
    private String corpo;
    private String imagemBase64;

    // likes/comentários
    private int likesCount;
    private int comentariosCount;

    private List<String> idLikes;

    // oportunidade
    private String idCategoria;
    private List<String> grandesAreas;
    private Integer quantidadeDeVagas;
    private Integer vagasPreenchidas;
    private Boolean finalizada;
    private StatusOportunidade status;

    private List<String> alunosCandidatosId;
    private List<String> alunosAprovadosId;
}