package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// Regras de permissão por perfil nos fluxos de oportunidade e candidatura (M1).
// O usuário vem sempre do JWT (email), nunca de um id na URL.
@Component
public class OportunidadePermissoes {

    private final UsuarioRepository usuarioRepository;
    private final OportunidadeRepository oportunidadeRepository;

    public OportunidadePermissoes(UsuarioRepository usuarioRepository, OportunidadeRepository oportunidadeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.oportunidadeRepository = oportunidadeRepository;
    }

    public Usuario usuarioAtual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Usuário não autenticado.");
        }
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    public Usuario exigirProfessor(Authentication authentication) {
        Usuario usuario = usuarioAtual(authentication);
        if (usuario.getFuncao() != FuncaoDeUsuario.PROFESSOR) {
            throw new SecurityException("Apenas professores podem gerenciar oportunidades.");
        }
        return usuario;
    }

    public Usuario exigirAlunoProprio(Authentication authentication, String idAluno) {
        Usuario usuario = usuarioAtual(authentication);
        if (usuario.getFuncao() != FuncaoDeUsuario.ALUNO) {
            throw new SecurityException("Apenas alunos podem se candidatar.");
        }
        if (!usuario.getId().equals(idAluno)) {
            throw new SecurityException("Você só pode se candidatar em seu próprio nome.");
        }
        return usuario;
    }

    public Usuario exigirProprioUsuario(Authentication authentication, String idUsuario) {
        Usuario usuario = usuarioAtual(authentication);
        if (!usuario.getId().equals(idUsuario)) {
            throw new SecurityException("Você não pode agir em nome de outro usuário.");
        }
        return usuario;
    }

    public Usuario exigirDono(Authentication authentication, String idOportunidade) {
        Usuario usuario = exigirProfessor(authentication);
        Oportunidade oportunidade = oportunidadeRepository.findById(idOportunidade)
                .orElseThrow(() -> new ResourceNotFoundException("Oportunidade não encontrada."));
        if (!usuario.getId().equals(oportunidade.getProfessorId())) {
            throw new SecurityException("Apenas o criador da oportunidade pode realizar esta ação.");
        }
        return usuario;
    }
}
