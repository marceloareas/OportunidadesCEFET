package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    Page<Post> findAllByOrderByCriadoDesc(Pageable pageable);

    Page<Post> findAllByCriadorIdOrderByCriadoDesc(String criadorId, Pageable pageable);
}