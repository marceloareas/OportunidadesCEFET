package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// Quem está logado vem sempre do JWT (email), nunca de um id na URL ou no corpo.
// Recusas viram SecurityException → 403 pelo ExceptionControllerAdvice.
@Component
public class PermissoesUsuario {

    private final UsuarioRepository usuarioRepository;

    public PermissoesUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario usuarioAtual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Usuário não autenticado.");
        }
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    public Usuario exigirProprioUsuario(Authentication authentication, String idUsuario) {
        Usuario usuario = usuarioAtual(authentication);
        if (!usuario.getId().equals(idUsuario)) {
            throw new SecurityException("Você não pode agir em nome de outro usuário.");
        }
        return usuario;
    }
}
