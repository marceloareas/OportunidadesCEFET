package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document("feed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedItem {

    @Id
    private String id;

    private String referenciaId; // id do Post ou Oportunidade
    private String tipo; // POST ou OPORTUNIDADE

    private String titulo;
    private String corpo;

    private String criadorId;
    private String nomeCriador;
    private Date criado;

    private String imagemBase64;
    private List<String> idLikes = new ArrayList<>();

    // campos extras de oportunidade
    private Integer quantidadeDeVagas;
    private Integer vagasPreenchidas;
    private Boolean finalizada;

    private List<String> alunosCandidatosId = new ArrayList<>();
    private List<String> alunosAprovadosId = new ArrayList<>();
}