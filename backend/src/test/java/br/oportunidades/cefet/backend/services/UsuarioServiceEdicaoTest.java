package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceEdicaoTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    // Sprint 5: aluno não vira professor editando o próprio perfil.
    @Test
    void edicaoNaoTrocaAFuncao() {
        Usuario aluno = Usuario.builder().id("u1").nome("Bia").email("bia@cefet.br")
                .funcao(FuncaoDeUsuario.ALUNO).build();
        when(usuarioRepository.findById("u1")).thenReturn(Optional.of(aluno));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario salvo = new UsuarioService(usuarioRepository, passwordEncoder)
                .updateUsuario("u1", Usuario.builder().nome("Bia Souza").funcao(FuncaoDeUsuario.PROFESSOR).build());

        assertEquals(FuncaoDeUsuario.ALUNO, salvo.getFuncao());
        assertEquals("Bia Souza", salvo.getNome());
    }
}
