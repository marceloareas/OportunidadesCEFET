package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerImagemTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    private MockMvc mvc;

    private final Usuario ana = Usuario.builder().id("u1").email("ana@cefet.br")
            .funcao(FuncaoDeUsuario.ALUNO).imagemPerfil("data:image/jpeg;base64,AAA").build();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
    }

    private UsernamePasswordAuthenticationToken logadoComo(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    @Test
    void donoRemoveAPropriaFoto() throws Exception {
        Usuario semFoto = Usuario.builder().id("u1").email("ana@cefet.br")
                .funcao(FuncaoDeUsuario.ALUNO).imagemPerfil(UsuarioService.AVATAR_ALUNO).build();
        when(usuarioService.getUsuarioById("u1")).thenReturn(ana);
        when(usuarioService.removerImagemPerfil("u1")).thenReturn(semFoto);

        mvc.perform(delete("/users/u1/imagem").principal(logadoComo("ana@cefet.br")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagemPerfil").value(UsuarioService.AVATAR_ALUNO));
    }

    @Test
    void outroUsuarioNaoPodeRemover() throws Exception {
        when(usuarioService.getUsuarioById("u1")).thenReturn(ana);

        mvc.perform(delete("/users/u1/imagem").principal(logadoComo("bruno@cefet.br")))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).removerImagemPerfil(anyString());
    }

    @Test
    void usuarioInexistenteRetorna404() throws Exception {
        when(usuarioService.getUsuarioById("x")).thenReturn(null);

        mvc.perform(delete("/users/x/imagem").principal(logadoComo("ana@cefet.br")))
                .andExpect(status().isNotFound());
    }
}
