// FeedController.java
package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.dto.feed.FeedPageDTO;
import br.oportunidades.cefet.backend.services.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feed")
@CrossOrigin(origins = "*")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping
    public FeedPageDTO listarFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String userId
    ) {
        return feedService.listarFeed(page, size, status, categoria, area, userId);
    }
}
