package br.oportunidades.cefet.backend.repositories;


import br.oportunidades.cefet.backend.models.FeedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FeedRepository extends MongoRepository<FeedItem, String> {

    Page<FeedItem> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<FeedItem> findByReferenciaId(String referenciaId);

    void deleteByReferenciaId(String referenciaId);
}
