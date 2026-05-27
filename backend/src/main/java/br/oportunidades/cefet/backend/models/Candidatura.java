package br.oportunidades.cefet.backend.models;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("Candidatura")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidatura {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Indexed
    private String alunoId;

    @Indexed
    private String oportunidadeId;

    @Builder.Default
    private StatusCandidatura status = StatusCandidatura.CONCORRENDO;

    @Builder.Default
    private Date criado = new Date();
}