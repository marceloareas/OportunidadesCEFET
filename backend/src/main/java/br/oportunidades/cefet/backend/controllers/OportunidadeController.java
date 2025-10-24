package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.services.OportunidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController()
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5173"})
@RequestMapping("/oportunities")
public class OportunidadeController {
    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    @GetMapping
    public ResponseEntity<List<Oportunidade>> listarTodas() {
        List<Oportunidade> oportunidades = oportunidadeService.listarTodas();
        return ResponseEntity.ok(oportunidades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Oportunidade> buscarPorId(@PathVariable String id) {
        return oportunidadeService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Oportunidade> criar(@RequestBody Oportunidade oportunidade) {
        Oportunidade criada = oportunidadeService.criar(oportunidade);
        return ResponseEntity.ok(criada);
    }

    @PostMapping("/{idOportunidade}/candidatar/{idAluno}")
    public ResponseEntity<Oportunidade> candidatar(
            @PathVariable String idOportunidade,
            @PathVariable String idAluno) {

        Oportunidade atualizada = oportunidadeService.candidatar(idOportunidade, idAluno);
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        oportunidadeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
