package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends MongoRepository<Categoria, String> {
}