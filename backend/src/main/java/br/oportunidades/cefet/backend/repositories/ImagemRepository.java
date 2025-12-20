package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Imagem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagemRepository extends MongoRepository<Imagem, String> {
}