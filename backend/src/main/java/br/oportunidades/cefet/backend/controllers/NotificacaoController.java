package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Notificacao;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoController(NotificacaoService notificacaoService, UsuarioRepository usuarioRepository) {
        this.notificacaoService = notificacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listar(Authentication authentication) {
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(usuarioAtualId(authentication)));
    }

    @GetMapping("/nao-lidas/contagem")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(Authentication authentication) {
        long total = notificacaoService.contarNaoLidas(usuarioAtualId(authentication));
        return ResponseEntity.ok(Map.of("total", total));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable String id, Authentication authentication) {
        notificacaoService.marcarComoLida(id, usuarioAtualId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(Authentication authentication) {
        notificacaoService.marcarTodasComoLidas(usuarioAtualId(authentication));
        return ResponseEntity.noContent().build();
    }

    // O usuário vem do JWT (email), nunca da URL: ninguém lê notificação de terceiros.
    private String usuarioAtualId(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .map(Usuario::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}
