package br.oportunidades.cefet.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("Categoria")
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Categoria {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String nome;
}
