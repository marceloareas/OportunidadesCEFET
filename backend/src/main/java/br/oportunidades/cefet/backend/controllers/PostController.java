package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.services.PermissoesUsuario;
import br.oportunidades.cefet.backend.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PermissoesUsuario permissoes;

    public PostController(PostService postService, PermissoesUsuario permissoes) {
        this.postService = postService;
        this.permissoes = permissoes;
    }

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

    // O autor é sempre quem está logado, nunca o criadorId enviado no corpo.
    @PostMapping
    public ResponseEntity<Post> criar(@RequestBody Post post, Authentication authentication) {
        Usuario autor = permissoes.usuarioAtual(authentication);
        post.setCriadorId(autor.getId());
        post.setNomeCriador(autor.getNome());
        return ResponseEntity.ok(postService.salvar(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id, Authentication authentication) {
        Post post = postService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicação não encontrada."));
        permissoes.exigirProprioUsuario(authentication, post.getCriadorId());
        postService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like/{idUsuario}")
    public ResponseEntity<String> alternarLike(@PathVariable String id, @PathVariable String idUsuario,
                                               Authentication authentication) {
        permissoes.exigirProprioUsuario(authentication, idUsuario);
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
