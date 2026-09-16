package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.exceptions.ConflictException;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceConflictTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createUsuarioLancaConflictExceptionQuandoEmailJaExiste() {
        UsuarioService usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);

        Usuario novoUsuario = Usuario.builder()
                .email("existente@cefet.br")
                .build();

        Usuario existente = Usuario.builder()
                .id("1")
                .email("existente@cefet.br")
                .build();

        when(usuarioRepository.findByEmail("existente@cefet.br")).thenReturn(Optional.of(existente));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> usuarioService.createUsuario(novoUsuario));

        assertEquals("Já existe um usuário cadastrado com esse e-mail.", ex.getMessage());
    }

    @Test
    void updateUsuarioLancaConflictExceptionQuandoEmailJaPertenceAOutroUsuario() {
        UsuarioService usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);

        Usuario existente = Usuario.builder()
                .id("1")
                .email("atual@cefet.br")
                .build();

        Usuario outro = Usuario.builder()
                .id("2")
                .email("existente@cefet.br")
                .build();

        Usuario atualizacao = Usuario.builder()
                .email("existente@cefet.br")
                .build();

        when(usuarioRepository.findById("1")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByEmail("existente@cefet.br")).thenReturn(Optional.of(outro));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> usuarioService.updateUsuario("1", atualizacao));

        assertEquals("Já existe outro usuário cadastrado com esse e-mail.", ex.getMessage());
    }
}
