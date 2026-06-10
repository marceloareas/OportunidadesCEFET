package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.repositories.SavedItemRepository;
import br.oportunidades.cefet.backend.repositories.FeedRepository;
import br.oportunidades.cefet.backend.dto.item_salvo.SavedItemDTO;
import br.oportunidades.cefet.backend.dto.feed.FeedResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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

    @Autowired
    private FeedService feedService;

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

    public Page<FeedResponseDTO> listar(String userId, Pageable pageable) {
        List<SavedItem> salvos = repository.findByUserId(userId);

        // feedItemId guarda o referenciaId (id da entidade Post/Oportunidade).
        List<String> referenciaIds = salvos.stream()
                .map(SavedItem::getFeedItemId)
                .toList();

        List<FeedItem> feedDocs = feedRepository.findByReferenciaIdIn(referenciaIds);

        // Reaproveita o enriquecimento do feed para que o card salvo tenha os mesmos dados.
        List<FeedResponseDTO> itens = feedDocs.stream()
                .map(feedService::montarFeedItem)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(itens, pageable, itens.size());
    }
}