package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.PermissoesUsuario;
import br.oportunidades.cefet.backend.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sprint 5: só o próprio usuário edita ou exclui o perfil.
@ExtendWith(MockitoExtension.class)
class UsuarioControllerPermissaoTest {

    @Mock private UsuarioService usuarioService;
    @Mock private UsuarioRepository usuarioRepository;

    private MockMvc mvc;

    private final Usuario ana = Usuario.builder()
            .id("u1").email("ana@cefet.br").funcao(FuncaoDeUsuario.ALUNO).build();

    @BeforeEach
    void setup() {
        UsuarioController controller = new UsuarioController();
        ReflectionTestUtils.setField(controller, "usuarioService", usuarioService);
        ReflectionTestUtils.setField(controller, "permissoes", new PermissoesUsuario(usuarioRepository));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
        when(usuarioRepository.findByEmail("ana@cefet.br")).thenReturn(Optional.of(ana));
    }

    private final UsernamePasswordAuthenticationToken comoAna =
            new UsernamePasswordAuthenticationToken("ana@cefet.br", null, List.of());

    @Test
    void editaOProprioPerfil() throws Exception {
        when(usuarioService.getUsuarioById("u1")).thenReturn(ana);
        when(usuarioService.updateUsuario(eq("u1"), any(Usuario.class))).thenReturn(ana);

        mvc.perform(put("/users/u1").principal(comoAna)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Ana Maria\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void naoEditaOPerfilDeOutro() throws Exception {
        mvc.perform(put("/users/u2").principal(comoAna)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"funcao\":\"PROFESSOR\"}"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).updateUsuario(anyString(), any());
    }

    @Test
    void naoExcluiOutroUsuario() throws Exception {
        mvc.perform(delete("/users/u2").principal(comoAna))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).deleteUsuario(anyString());
    }

    @Test
    void excluiAPropriaConta() throws Exception {
        when(usuarioService.getUsuarioById("u1")).thenReturn(ana);

        mvc.perform(delete("/users/u1").principal(comoAna))
                .andExpect(status().isNoContent());

        verify(usuarioService).deleteUsuario("u1");
    }
}
