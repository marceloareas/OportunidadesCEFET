package br.oportunidades.cefet.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import br.oportunidades.cefet.backend.models.SavedItem;

@Repository
public interface SavedItemRepository extends MongoRepository<SavedItem, String> {

    List<SavedItem> findByUserId(String userId);

    Optional<SavedItem> findByUserIdAndFeedItemId(String userId, String feedItemId);

    void deleteByUserIdAndFeedItemId(String userId, String feedItemId);
}