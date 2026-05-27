package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Candidatura;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CandidaturaRepository
        extends MongoRepository<Candidatura, String> {

    List<Candidatura> findByAlunoId(String alunoId);

    List<Candidatura> findByOportunidadeId(String oportunidadeId);

    Optional<Candidatura> findByAlunoIdAndOportunidadeId(
            String alunoId,
            String oportunidadeId
    );
}