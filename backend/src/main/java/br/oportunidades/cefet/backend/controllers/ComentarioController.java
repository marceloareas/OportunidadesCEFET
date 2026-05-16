package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.services.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

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

    @PostMapping
    public ResponseEntity<Comentario> criar(@RequestBody Comentario comentario) {
        return ResponseEntity.ok(comentarioService.salvar(comentario));
    }
}
