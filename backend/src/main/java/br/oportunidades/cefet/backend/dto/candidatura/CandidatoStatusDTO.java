package br.oportunidades.cefet.backend.dto.candidatura;

import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import br.oportunidades.cefet.backend.models.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidatoStatusDTO {

    private Usuario aluno;
    private StatusCandidatura status;
}
