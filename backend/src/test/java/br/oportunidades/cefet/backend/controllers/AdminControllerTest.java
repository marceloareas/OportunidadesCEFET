package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.AdminService;
import br.oportunidades.cefet.backend.services.PermissoesUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sprint 6: só o administrador lista e desativa usuários.
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private MockMvc mvc;

    private final Usuario admin = Usuario.builder().id("adm").nome("Administrador")
            .email("admin@cefet-rj.br").funcao(FuncaoDeUsuario.ADMIN).ativo(true).build();
    private final Usuario outroAdmin = Usuario.builder().id("adm2").email("adm2@cefet-rj.br")
            .funcao(FuncaoDeUsuario.ADMIN).build();
    private final Usuario bia = Usuario.builder().id("aluno-1").nome("Bia")
            .email("bia@cefet.br").funcao(FuncaoDeUsuario.ALUNO).build();

    private final UsernamePasswordAuthenticationToken comoAdmin =
            new UsernamePasswordAuthenticationToken("admin@cefet-rj.br", null, List.of());
    private final UsernamePasswordAuthenticationToken comoBia =
            new UsernamePasswordAuthenticationToken("bia@cefet.br", null, List.of());

    @BeforeEach
    void setup() {
        PermissoesUsuario permissoes = new PermissoesUsuario(usuarioRepository);
        mvc = MockMvcBuilders
                .standaloneSetup(new AdminController(new AdminService(usuarioRepository), permissoes))
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
        lenient().when(usuarioRepository.findByEmail("admin@cefet-rj.br")).thenReturn(Optional.of(admin));
        lenient().when(usuarioRepository.findByEmail("bia@cefet.br")).thenReturn(Optional.of(bia));
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void alunoNaoListaUsuarios() throws Exception {
        mvc.perform(get("/admin/usuarios").principal(comoBia)).andExpect(status().isForbidden());
        verify(usuarioRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void adminListaUsuariosPaginados() throws Exception {
        when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(admin, bia), PageRequest.of(0, 10), 2));

        mvc.perform(get("/admin/usuarios").principal(comoAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[1].email").value("bia@cefet.br"))
                .andExpect(jsonPath("$.content[1].ativo").value(true))
                .andExpect(jsonPath("$.content[1].senha").doesNotExist());
    }

    @Test
    void adminBuscaPorNomeOuEmail() throws Exception {
        when(usuarioRepository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
                eq("bia"), eq("bia"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(bia), PageRequest.of(0, 10), 1));

        mvc.perform(get("/admin/usuarios").param("busca", " bia ").principal(comoAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("aluno-1"));
    }

    @Test
    void adminDesativaAluno() throws Exception {
        when(usuarioRepository.findById("aluno-1")).thenReturn(Optional.of(bia));

        mvc.perform(patch("/admin/usuarios/aluno-1/desativar").principal(comoAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        assertFalse(bia.getAtivo());
        verify(usuarioRepository).save(bia);
    }

    @Test
    void adminReativaAluno() throws Exception {
        bia.setAtivo(false);
        when(usuarioRepository.findById("aluno-1")).thenReturn(Optional.of(bia));

        mvc.perform(patch("/admin/usuarios/aluno-1/reativar").principal(comoAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));

        assertTrue(bia.getAtivo());
    }

    @Test
    void adminNaoDesativaASiMesmo() throws Exception {
        when(usuarioRepository.findById("adm")).thenReturn(Optional.of(admin));

        mvc.perform(patch("/admin/usuarios/adm/desativar").principal(comoAdmin))
                .andExpect(status().isForbidden());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void adminNaoDesativaOutroAdmin() throws Exception {
        when(usuarioRepository.findById("adm2")).thenReturn(Optional.of(outroAdmin));

        mvc.perform(patch("/admin/usuarios/adm2/desativar").principal(comoAdmin))
                .andExpect(status().isForbidden());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void desativarUsuarioInexistenteDa404() throws Exception {
        when(usuarioRepository.findById("nada")).thenReturn(Optional.empty());

        mvc.perform(patch("/admin/usuarios/nada/desativar").principal(comoAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void alunoNaoDesativaNinguem() throws Exception {
        mvc.perform(patch("/admin/usuarios/aluno-1/desativar").principal(comoBia))
                .andExpect(status().isForbidden());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
