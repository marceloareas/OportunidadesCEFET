package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.services.OportunidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/oportunidades")
@CrossOrigin(origins = "*")
public class OportunidadeController {

    @Autowired
    private OportunidadeService oportunidadeService;

    @GetMapping
    public List<Oportunidade> listar() {
        return oportunidadeService.listarTodos();
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


}
