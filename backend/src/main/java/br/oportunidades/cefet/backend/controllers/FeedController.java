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
            @RequestParam(defaultValue = "20") int size
    ) {
        return feedService.listarFeed(page, size);
    }
}