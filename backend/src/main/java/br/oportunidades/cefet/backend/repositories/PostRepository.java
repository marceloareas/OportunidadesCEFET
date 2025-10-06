package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
}
