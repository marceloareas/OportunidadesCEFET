package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.services.OportunidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/oportunidades")
public class OportunidadeController {

    @Autowired
    private OportunidadeService oportunidadeService;

    @GetMapping
    public ResponseEntity<Page<Oportunidade>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(oportunidadeService.listarTodos(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Oportunidade> buscarPorId(@PathVariable String id) {
        return oportunidadeService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Oportunidade> criar(@RequestBody Oportunidade oportunidade) {
        try {
            Oportunidade salva = oportunidadeService.salvar(oportunidade);
            return ResponseEntity.ok(salva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Oportunidade> atualizar(@PathVariable String id, @RequestBody Oportunidade atualizada) {
        return oportunidadeService.atualizar(id, atualizada)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        try {
            oportunidadeService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/candidatar/{idAluno}")
    public ResponseEntity<?> candidatar(@PathVariable String id, @PathVariable String idAluno) {
        Optional<Oportunidade> resultado = oportunidadeService.candidatar(id, idAluno);
        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resultado.get());
    }

    @PostMapping("/{id}/like/{idUsuario}")
    public ResponseEntity<String> alternarLike(@PathVariable String id, @PathVariable String idUsuario) {
        String resultado = oportunidadeService.alternarLike(id, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable String id) {
        Optional<Oportunidade> opt = oportunidadeService.buscarPorId(id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Oportunidade oportunidade = opt.get();
        oportunidade.setFinalizada(true);
        oportunidadeService.salvar(oportunidade);

        return ResponseEntity.ok(oportunidade);
    }

    @GetMapping("/{id}/candidatos")
    public ResponseEntity<Page<Usuario>> listarCandidatos(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                oportunidadeService.listarCandidatos(id, page, size)
        );
    }

    // Lista candidatos apenas se o professor for o dono da oportunidade
    @GetMapping("/{id}/candidatos/professor/{idProfessor}")
    public ResponseEntity<?> listarCandidatosDoProfessor(
            @PathVariable String id,
            @PathVariable String idProfessor) {
        try {
            return oportunidadeService.listarCandidatosDoProfessor(id, idProfessor)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }

    @GetMapping("/professor/{idProfessor}")
    public ResponseEntity<Page<Oportunidade>> listarPorProfessor(
            @PathVariable String idProfessor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                oportunidadeService.listarPorProfessor(idProfessor, page, size)
        );
    }

    @GetMapping("/aluno/{idAluno}")
    public ResponseEntity<Page<Oportunidade>> listarPorAluno(
            @PathVariable String idAluno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                oportunidadeService.listarPorAluno(idAluno, page, size)
        );
    }

    @PostMapping("/{id}/aprovar/{idAluno}")
    public ResponseEntity<?> aprovarCandidato(
            @PathVariable String id,
            @PathVariable String idAluno) {

        try {
            return oportunidadeService.aprovarCandidato(id, idAluno)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Aprovar candidato informando o professor dono
    @PostMapping("/{id}/aprovar/{idAluno}/professor/{idProfessor}")
    public ResponseEntity<?> aprovarCandidatoDoProfessor(
            @PathVariable String id,
            @PathVariable String idAluno,
            @PathVariable String idProfessor) {

        try {
            return oportunidadeService.aprovarCandidatoDoProfessor(id, idAluno, idProfessor)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
