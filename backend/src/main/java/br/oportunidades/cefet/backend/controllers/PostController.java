package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<Page<Post>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postService.listarTodos(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> buscarPorId(@PathVariable String id) {
        return postService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Post> criar(@RequestBody Post post) {
        return ResponseEntity.ok(postService.salvar(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        postService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like/{idUsuario}")
    public ResponseEntity<String> alternarLike(@PathVariable String id, @PathVariable String idUsuario) {
        String resultado = postService.alternarLike(id, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/mine/{userId}")
    public ResponseEntity<Page<Post>> listarPorUsuario(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.listarPorUsuario(userId, page, size));
    }


}
