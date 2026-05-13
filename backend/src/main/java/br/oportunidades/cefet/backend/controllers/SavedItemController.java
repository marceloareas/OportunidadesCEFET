package br.oportunidades.cefet.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.oportunidades.cefet.backend.dto.item_salvo.SavedItemDTO;
import br.oportunidades.cefet.backend.models.FeedItem;
import br.oportunidades.cefet.backend.services.SavedItemService;

@RestController
@RequestMapping("/saved-items")
@CrossOrigin
public class SavedItemController {

    @Autowired
    private SavedItemService service;

    @PostMapping
    public ResponseEntity<Void> salvar(@RequestBody SavedItemDTO dto) {
        service.salvar(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{feedItemId}")
    public ResponseEntity<Void> remover(
            @PathVariable String userId,
            @PathVariable String feedItemId) {

        service.remover(userId, feedItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<FeedItem>> listar(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listar(userId, pageable));
    }
}