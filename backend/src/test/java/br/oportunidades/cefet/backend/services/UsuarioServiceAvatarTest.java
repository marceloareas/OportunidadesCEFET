package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceAvatarTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UsuarioService service;

    @BeforeEach
    void setup() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    private void salvarDevolveOArgumento() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void cadastroDeAlunoSemFotoRecebeAvatarDeAluno() {
        salvarDevolveOArgumento();

        Usuario salvo = service.createUsuario(
                Usuario.builder().email("a@cefet.br").funcao(FuncaoDeUsuario.ALUNO).build());

        assertEquals(UsuarioService.AVATAR_ALUNO, salvo.getImagemPerfil());
    }

    @Test
    void cadastroDeProfessorSemFotoRecebeAvatarDeProfessor() {
        salvarDevolveOArgumento();

        Usuario salvo = service.createUsuario(
                Usuario.builder().email("p@cefet.br").funcao(FuncaoDeUsuario.PROFESSOR).build());

        assertEquals(UsuarioService.AVATAR_PROFESSOR, salvo.getImagemPerfil());
    }

    @Test
    void cadastroComFotoEmBrancoRecebeAvatarPadrao() {
        salvarDevolveOArgumento();

        Usuario salvo = service.createUsuario(
                Usuario.builder().email("a@cefet.br").funcao(FuncaoDeUsuario.ALUNO).imagemPerfil("  ").build());

        assertEquals(UsuarioService.AVATAR_ALUNO, salvo.getImagemPerfil());
    }

    @Test
    void cadastroComFotoMantemAFoto() {
        salvarDevolveOArgumento();

        Usuario salvo = service.createUsuario(Usuario.builder().email("a@cefet.br")
                .funcao(FuncaoDeUsuario.ALUNO).imagemPerfil("data:image/jpeg;base64,AAA").build());

        assertEquals("data:image/jpeg;base64,AAA", salvo.getImagemPerfil());
    }

    @Test
    void updateComImagemVaziaVoltaParaOPadrao() {
        Usuario existente = Usuario.builder().id("u1").email("p@cefet.br")
                .funcao(FuncaoDeUsuario.PROFESSOR).imagemPerfil("data:image/jpeg;base64,AAA").build();
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(existente));
        salvarDevolveOArgumento();

        Usuario atualizado = service.updateUsuario("u1", Usuario.builder().imagemPerfil("").build());

        assertEquals(UsuarioService.AVATAR_PROFESSOR, atualizado.getImagemPerfil());
    }

    @Test
    void removerImagemPerfilVoltaParaOPadrao() {
        Usuario existente = Usuario.builder().id("u1").email("a@cefet.br")
                .funcao(FuncaoDeUsuario.ALUNO).imagemPerfil("data:image/jpeg;base64,AAA").build();
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(existente));
        salvarDevolveOArgumento();

        Usuario atualizado = service.removerImagemPerfil("u1");

        assertEquals(UsuarioService.AVATAR_ALUNO, atualizado.getImagemPerfil());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void removerImagemPerfilDeUsuarioInexistenteLancaNotFound() {
        when(usuarioRepository.findById("x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.removerImagemPerfil("x"));
    }

    @Test
    void preencherImagensPadraoAtualizaQuemNaoTemFoto() {
        Usuario aluno = Usuario.builder().id("a").funcao(FuncaoDeUsuario.ALUNO).build();
        Usuario professor = Usuario.builder().id("p").funcao(FuncaoDeUsuario.PROFESSOR).imagemPerfil("").build();
        List<Usuario> semFoto = List.of(aluno, professor);
        when(usuarioRepository.findByImagemPerfilIsNullOrImagemPerfil("")).thenReturn(semFoto);

        int atualizados = service.preencherImagensPadrao();

        assertEquals(2, atualizados);
        assertEquals(UsuarioService.AVATAR_ALUNO, aluno.getImagemPerfil());
        assertEquals(UsuarioService.AVATAR_PROFESSOR, professor.getImagemPerfil());
        verify(usuarioRepository).saveAll(semFoto);
    }
}
