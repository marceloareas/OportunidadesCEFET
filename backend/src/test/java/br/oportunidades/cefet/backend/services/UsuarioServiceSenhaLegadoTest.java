package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceSenhaLegadoTest {

    @Mock private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsuarioService service;

    @BeforeEach
    void setup() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @SuppressWarnings("unchecked")
    private List<Usuario> salvos() {
        ArgumentCaptor<List<Usuario>> captor = ArgumentCaptor.forClass(List.class);
        verify(usuarioRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void senhaEmTextoPuroViraBcrypt() {
        Usuario legado = Usuario.builder().email("a@cefet.br").senha("123456").build();
        when(usuarioRepository.findAll()).thenReturn(List.of(legado));

        int migrados = service.migrarSenhasLegado();

        assertEquals(1, migrados);
        assertTrue(UsuarioService.ehHashBcrypt(legado.getSenha()));
        assertTrue(passwordEncoder.matches("123456", legado.getSenha()));
    }

    @Test
    void senhaJaEmBcryptNaoEMexida() {
        String hash = passwordEncoder.encode("123456");
        Usuario atual = Usuario.builder().email("b@cefet.br").senha(hash).build();
        when(usuarioRepository.findAll()).thenReturn(List.of(atual));

        assertEquals(0, service.migrarSenhasLegado());
        assertEquals(hash, atual.getSenha());
        assertTrue(salvos().isEmpty());
    }

    @Test
    void usuarioSemSenhaEIgnorado() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                Usuario.builder().email("c@cefet.br").build(),
                Usuario.builder().email("d@cefet.br").senha("").build()));

        assertEquals(0, service.migrarSenhasLegado());
        assertTrue(salvos().isEmpty());
    }

    @Test
    void rodarDuasVezesNaoReprocessa() {
        Usuario legado = Usuario.builder().email("e@cefet.br").senha("abc").build();
        when(usuarioRepository.findAll()).thenReturn(List.of(legado));

        assertEquals(1, service.migrarSenhasLegado());
        String hash = legado.getSenha();
        assertEquals(0, service.migrarSenhasLegado());
        assertEquals(hash, legado.getSenha());
    }

    @Test
    void reconheceOFormatoDoHash() {
        assertTrue(UsuarioService.ehHashBcrypt(passwordEncoder.encode("x")));
        assertFalse(UsuarioService.ehHashBcrypt("$2a$10$curto"));
        assertFalse(UsuarioService.ehHashBcrypt("senha123"));
        assertFalse(UsuarioService.ehHashBcrypt(null));
    }
}
