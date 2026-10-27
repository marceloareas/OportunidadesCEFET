package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.services.OportunidadePermissoes;
import br.oportunidades.cefet.backend.services.OportunidadeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/oportunidades")
public class OportunidadeController {

    private final OportunidadeService oportunidadeService;
    private final OportunidadePermissoes permissoes;

    public OportunidadeController(OportunidadeService oportunidadeService, OportunidadePermissoes permissoes) {
        this.oportunidadeService = oportunidadeService;
        this.permissoes = permissoes;
    }

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

    // Só professor cria, e a oportunidade fica sempre em nome de quem está logado.
    @PostMapping
    public ResponseEntity<Oportunidade> criar(@RequestBody Oportunidade oportunidade, Authentication authentication) {
        Usuario professor = permissoes.exigirProfessor(authentication);
        oportunidade.setProfessorId(professor.getId());
        Oportunidade salva = oportunidadeService.salvar(oportunidade);
        return ResponseEntity.ok(salva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Oportunidade> atualizar(@PathVariable String id, @RequestBody Oportunidade atualizada,
                                                  Authentication authentication) {
        permissoes.exigirDono(authentication, id);
        return oportunidadeService.atualizar(id, atualizada)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id, Authentication authentication) {
        permissoes.exigirDono(authentication, id);
        oportunidadeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/candidatar/{idAluno}")
    public ResponseEntity<?> candidatar(@PathVariable String id, @PathVariable String idAluno,
                                        Authentication authentication) {
        permissoes.exigirAlunoProprio(authentication, idAluno);
        Optional<Oportunidade> resultado = oportunidadeService.candidatar(id, idAluno);
        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resultado.get());
    }

    @PostMapping("/{id}/like/{idUsuario}")
    public ResponseEntity<String> alternarLike(@PathVariable String id, @PathVariable String idUsuario,
                                               Authentication authentication) {
        permissoes.exigirProprioUsuario(authentication, idUsuario);
        String resultado = oportunidadeService.alternarLike(id, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable String id, Authentication authentication) {
        permissoes.exigirDono(authentication, id);
        return oportunidadeService.finalizar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/candidatos")
    public ResponseEntity<Page<Usuario>> listarCandidatos(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        permissoes.exigirDono(authentication, id);
        return ResponseEntity.ok(
                oportunidadeService.listarCandidatos(id, page, size)
        );
    }

    // Lista candidatos apenas se o professor for o dono da oportunidade
    @GetMapping("/{id}/candidatos/professor/{idProfessor}")
    public ResponseEntity<?> listarCandidatosDoProfessor(
            @PathVariable String id,
            @PathVariable String idProfessor,
            Authentication authentication) {
        permissoes.exigirProprioUsuario(authentication, idProfessor);
        return oportunidadeService.listarCandidatosDoProfessor(id, idProfessor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
            @PathVariable String idAluno,
            Authentication authentication) {
        Usuario professor = permissoes.exigirDono(authentication, id);
        return oportunidadeService.aprovarCandidatoDoProfessor(id, idAluno, professor.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Aprovar candidato informando o professor dono
    @PostMapping("/{id}/aprovar/{idAluno}/professor/{idProfessor}")
    public ResponseEntity<?> aprovarCandidatoDoProfessor(
            @PathVariable String id,
            @PathVariable String idAluno,
            @PathVariable String idProfessor,
            Authentication authentication) {
        permissoes.exigirProprioUsuario(authentication, idProfessor);
        permissoes.exigirDono(authentication, id);
        return oportunidadeService.aprovarCandidatoDoProfessor(id, idAluno, idProfessor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
