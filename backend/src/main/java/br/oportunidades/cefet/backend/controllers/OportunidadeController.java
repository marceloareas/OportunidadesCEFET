package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.services.OportunidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController()
@RequestMapping("/oportunities")
public class OportunidadeController {
    private final OportunidadeService oportunidadeService;

    // ✅ Injeção de dependência
    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    // 🟩 1. Listar todas as oportunidades (aluno/professor)
    @GetMapping
    public ResponseEntity<List<Oportunidade>> listarTodas() {
        List<Oportunidade> oportunidades = oportunidadeService.listarTodas();
        return ResponseEntity.ok(oportunidades);
    }

    // 🟦 2. Buscar uma oportunidade por ID
    @GetMapping("/{id}")
    public ResponseEntity<Oportunidade> buscarPorId(@PathVariable String id) {
        return oportunidadeService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🟨 3. Criar nova oportunidade (professor)
    @PostMapping
    public ResponseEntity<Oportunidade> criar(@RequestBody Oportunidade oportunidade) {
        Oportunidade criada = oportunidadeService.criar(oportunidade);
        return ResponseEntity.ok(criada);
    }

    // 🟪 4. Candidatar um aluno
    @PostMapping("/{idOportunidade}/candidatar/{idAluno}")
    public ResponseEntity<Oportunidade> candidatar(
            @PathVariable String idOportunidade,
            @PathVariable String idAluno) {

        Oportunidade atualizada = oportunidadeService.candidatar(idOportunidade, idAluno);
        return ResponseEntity.ok(atualizada);
    }

    // 🟥 5. Deletar uma oportunidade
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        oportunidadeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
