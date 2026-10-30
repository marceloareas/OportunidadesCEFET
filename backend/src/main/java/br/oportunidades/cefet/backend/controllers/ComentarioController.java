package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.services.ComentarioService;
import br.oportunidades.cefet.backend.services.PermissoesUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final PermissoesUsuario permissoes;

    public ComentarioController(ComentarioService comentarioService, PermissoesUsuario permissoes) {
        this.comentarioService = comentarioService;
        this.permissoes = permissoes;
    }

    // 🔹 Comentários de um Post
    @GetMapping("/post/{idPost}")
    public ResponseEntity<List<Comentario>> listarComentariosPost(
            @PathVariable String idPost
    ) {
        return ResponseEntity.ok(
                comentarioService.listarComentariosDePost(idPost)
        );
    }

    // 🔹 Comentários de uma Oportunidade
    @GetMapping("/oportunidade/{idOportunidade}")
    public ResponseEntity<List<Comentario>> listarComentariosOportunidade(
            @PathVariable String idOportunidade
    ) {
        return ResponseEntity.ok(
                comentarioService.listarComentariosDeOportunidade(idOportunidade)
        );
    }

    // O autor do comentário é sempre quem está logado, nunca o usuarioId enviado no corpo.
    @PostMapping
    public ResponseEntity<Comentario> criar(@RequestBody Comentario comentario, Authentication authentication) {
        comentario.setUsuarioId(permissoes.usuarioAtual(authentication).getId());
        return ResponseEntity.ok(comentarioService.salvar(comentario));
    }
}
