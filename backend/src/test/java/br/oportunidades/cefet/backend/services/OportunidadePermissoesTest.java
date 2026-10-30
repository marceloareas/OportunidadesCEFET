package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OportunidadePermissoesTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;

    private OportunidadePermissoes permissoes;

    private final Usuario professor = Usuario.builder()
            .id("prof-1").email("prof@cefet.br").funcao(FuncaoDeUsuario.PROFESSOR).build();
    private final Usuario outroProfessor = Usuario.builder()
            .id("prof-2").email("outro@cefet.br").funcao(FuncaoDeUsuario.PROFESSOR).build();
    private final Usuario aluno = Usuario.builder()
            .id("aluno-1").email("aluno@cefet.br").funcao(FuncaoDeUsuario.ALUNO).build();

    @BeforeEach
    void setup() {
        permissoes = new OportunidadePermissoes(usuarioRepository, oportunidadeRepository);
    }

    private Authentication logado(Usuario usuario) {
        lenient().when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        return new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of());
    }

    private void oportunidadeDe(String professorId) {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(
                Oportunidade.builder().id("op-1").professorId(professorId).build()));
    }

    @Test
    void semAutenticacaoEhNegado() {
        assertThrows(SecurityException.class, () -> permissoes.usuarioAtual(null));
    }

    @Test
    void tokenDeUsuarioInexistenteDa404() {
        Authentication auth = new UsernamePasswordAuthenticationToken("sumiu@cefet.br", null, List.of());
        when(usuarioRepository.findByEmail("sumiu@cefet.br")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permissoes.usuarioAtual(auth));
    }

    @Test
    void professorPodeCriar() {
        assertEquals(professor, permissoes.exigirProfessor(logado(professor)));
    }

    @Test
    void alunoNaoPodeCriarOportunidade() {
        assertThrows(SecurityException.class, () -> permissoes.exigirProfessor(logado(aluno)));
    }

    @Test
    void alunoSeCandidataEmNomeProprio() {
        assertEquals(aluno, permissoes.exigirAlunoProprio(logado(aluno), "aluno-1"));
    }

    @Test
    void alunoNaoCandidataOutroAluno() {
        assertThrows(SecurityException.class, () -> permissoes.exigirAlunoProprio(logado(aluno), "aluno-2"));
    }

    @Test
    void professorNaoSeCandidata() {
        assertThrows(SecurityException.class, () -> permissoes.exigirAlunoProprio(logado(professor), "prof-1"));
    }

    @Test
    void donoPodeGerenciarAOportunidade() {
        oportunidadeDe("prof-1");

        assertEquals(professor, permissoes.exigirDono(logado(professor), "op-1"));
    }

    @Test
    void outroProfessorNaoGerencia() {
        oportunidadeDe("prof-1");

        assertThrows(SecurityException.class, () -> permissoes.exigirDono(logado(outroProfessor), "op-1"));
    }

    @Test
    void alunoNaoGerenciaMesmoSabendoOId() {
        assertThrows(SecurityException.class, () -> permissoes.exigirDono(logado(aluno), "op-1"));
        verifyNoInteractions(oportunidadeRepository);
    }

    @Test
    void oportunidadeInexistenteDa404() {
        when(oportunidadeRepository.findById("op-x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permissoes.exigirDono(logado(professor), "op-x"));
    }

    @Test
    void naoAgeEmNomeDeOutroUsuario() {
        assertEquals(aluno, permissoes.exigirProprioUsuario(logado(aluno), "aluno-1"));
        assertThrows(SecurityException.class, () -> permissoes.exigirProprioUsuario(logado(aluno), "prof-1"));
    }
}
