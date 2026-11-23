package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Sprint 6: perfil administrador criado na subida e bloqueado no cadastro público.
@ExtendWith(MockitoExtension.class)
class UsuarioServiceAdminTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UsuarioService service;

    @BeforeEach
    void setup() {
        service = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void criaAdminQuandoNaoExiste() {
        when(usuarioRepository.findByEmail("admin@cefet-rj.br")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.garantirAdmin("admin@cefet-rj.br", "Admin@2026", "Administrador"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario admin = captor.getValue();
        assertEquals(FuncaoDeUsuario.ADMIN, admin.getFuncao());
        assertEquals("Administrador", admin.getNome());
        assertTrue(admin.getAtivo());
        assertNotNull(admin.getCriado());
        assertTrue(passwordEncoder.matches("Admin@2026", admin.getSenha()));
    }

    @Test
    void naoDuplicaNemTrocaSenhaDoAdminExistente() {
        String hash = passwordEncoder.encode("senha-trocada");
        Usuario existente = Usuario.builder().id("adm").email("admin@cefet-rj.br")
                .funcao(FuncaoDeUsuario.ADMIN).ativo(true).senha(hash).build();
        when(usuarioRepository.findByEmail("admin@cefet-rj.br")).thenReturn(Optional.of(existente));

        assertFalse(service.garantirAdmin("admin@cefet-rj.br", "Admin@2026", "Administrador"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        assertEquals(hash, existente.getSenha());
    }

    // Revisão: quem se cadastrou antes com o e-mail do admin não pode virar administrador na subida.
    @Test
    void naoPromoveContaComumQueUsaOEmailDoAdmin() {
        Usuario existente = Usuario.builder().id("u1").email("admin@cefet-rj.br")
                .funcao(FuncaoDeUsuario.ALUNO).senha("hash").build();
        when(usuarioRepository.findByEmail("admin@cefet-rj.br")).thenReturn(Optional.of(existente));

        assertFalse(service.garantirAdmin("admin@cefet-rj.br", "Admin@2026", "Administrador"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        assertEquals(FuncaoDeUsuario.ALUNO, existente.getFuncao());
    }

    @Test
    void reativaAdminQueEstavaDesativadoNoBancoSemTrocarSenha() {
        String hash = passwordEncoder.encode("senha-trocada");
        Usuario existente = Usuario.builder().id("adm").email("admin@cefet-rj.br")
                .funcao(FuncaoDeUsuario.ADMIN).ativo(false).senha(hash).build();
        when(usuarioRepository.findByEmail("admin@cefet-rj.br")).thenReturn(Optional.of(existente));

        assertTrue(service.garantirAdmin("admin@cefet-rj.br", "Admin@2026", "Administrador"));

        verify(usuarioRepository).save(existente);
        assertTrue(existente.getAtivo());
        assertEquals(hash, existente.getSenha());
    }

    // Revisão: o cadastro público não pode escolher o id (sobrescreveria/reativaria outra conta).
    @Test
    void cadastroPublicoIgnoraIdEDadosInternosEnviadosNoCorpo() {
        when(usuarioRepository.findByEmail("x@cefet.br")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        Usuario tentativa = Usuario.builder().id("id-de-outra-pessoa").email("x@cefet.br").senha("123")
                .funcao(FuncaoDeUsuario.ALUNO).idCandidaturas(new java.util.ArrayList<>(java.util.List.of("op-1")))
                .build();

        Usuario salvo = service.createUsuario(tentativa);

        assertNotEquals("id-de-outra-pessoa", salvo.getId());
        assertTrue(salvo.getIdCandidaturas().isEmpty());
    }

    @Test
    void cadastroPublicoNaoCriaAdmin() {
        Usuario tentativa = Usuario.builder().email("x@cefet.br").senha("123")
                .funcao(FuncaoDeUsuario.ADMIN).build();

        assertThrows(SecurityException.class, () -> service.createUsuario(tentativa));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cadastroPublicoIgnoraAtivoFalsoEnviadoNoCorpo() {
        when(usuarioRepository.findByEmail("x@cefet.br")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        Usuario novo = Usuario.builder().email("x@cefet.br").senha("123")
                .funcao(FuncaoDeUsuario.ALUNO).ativo(false).build();

        assertTrue(service.createUsuario(novo).getAtivo());
    }

    @Test
    void usuarioLegadoSemCampoAtivoContaComoAtivo() {
        Usuario legado = Usuario.builder().email("velho@cefet.br").build();
        assertTrue(legado.getAtivo());
    }
}
