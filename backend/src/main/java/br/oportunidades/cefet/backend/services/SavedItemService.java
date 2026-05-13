package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.repositories.SavedItemRepository;
import br.oportunidades.cefet.backend.repositories.FeedRepository;
import br.oportunidades.cefet.backend.dto.item_salvo.SavedItemDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import br.oportunidades.cefet.backend.models.FeedItem;
import br.oportunidades.cefet.backend.models.SavedItem;

@Service
public class SavedItemService {

    @Autowired
    private SavedItemRepository repository;

    @Autowired
    private FeedRepository feedRepository; 

    public void salvar(SavedItemDTO dto) {
        Optional<SavedItem> existente =
            repository.findByUserIdAndFeedItemId(dto.getUserId(), dto.getFeedItemId());

        if (existente.isEmpty()) {
            repository.save(new SavedItem(dto.getUserId(), dto.getFeedItemId()));
        }
    }

    public void remover(String userId, String feedItemId) {
        repository.deleteByUserIdAndFeedItemId(userId, feedItemId);
    }

    public Page<FeedItem> listar(String userId, Pageable pageable) {
        List<SavedItem> salvos = repository.findByUserId(userId);

        List<String> ids = salvos.stream()
                .map(SavedItem::getFeedItemId)
                .toList();

        List<FeedItem> feed = feedRepository.findAllById(ids);

        return new PageImpl<>(feed, pageable, feed.size());
    }
}