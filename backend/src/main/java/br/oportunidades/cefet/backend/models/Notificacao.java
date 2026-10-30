package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("notificacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacao {

    @Id
    private String id;

    @Indexed
    private String usuarioId;

    private TipoNotificacao tipo;
    private String mensagem;
    private String referenciaId;

    private boolean lida;

    @Builder.Default
    private Date criadoEm = new Date();
}
