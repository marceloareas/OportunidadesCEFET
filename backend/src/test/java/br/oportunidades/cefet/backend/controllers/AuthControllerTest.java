package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(new AuthController(usuarioRepository, passwordEncoder, jwtService))
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
    }

    private Usuario ana(String senhaGravada) {
        return Usuario.builder().id("user-1").nome("Ana").email("ana@cefet.br")
                .funcao(FuncaoDeUsuario.ALUNO).senha(senhaGravada).build();
    }

    private String corpoLogin(String email, String senha) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    @Test
    void loginComSenhaCorretaDevolveTokenEUsuario() throws Exception {
        when(usuarioRepository.findByEmail("ana@cefet.br"))
                .thenReturn(Optional.of(ana(passwordEncoder.encode("segredo"))));
        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("jwt-falso");

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("ana@cefet.br", "segredo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-falso"))
                .andExpect(jsonPath("$.usuario.email").value("ana@cefet.br"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    void loginComSenhaErradaDa401() throws Exception {
        when(usuarioRepository.findByEmail("ana@cefet.br"))
                .thenReturn(Optional.of(ana(passwordEncoder.encode("segredo"))));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("ana@cefet.br", "errada")))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jwtService);
    }

    @Test
    void emailInexistenteDa401() throws Exception {
        when(usuarioRepository.findByEmail("nao@cefet.br")).thenReturn(Optional.empty());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("nao@cefet.br", "x")))
                .andExpect(status().isUnauthorized());
    }

    // M5: o login não aceita mais senha gravada em texto puro.
    @Test
    void senhaEmTextoPuroNaoEhMaisAceita() throws Exception {
        when(usuarioRepository.findByEmail("ana@cefet.br")).thenReturn(Optional.of(ana("segredo")));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("ana@cefet.br", "segredo")))
                .andExpect(status().isUnauthorized());

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void usuarioSemSenhaGravadaDa401() throws Exception {
        when(usuarioRepository.findByEmail("ana@cefet.br")).thenReturn(Optional.of(ana(null)));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("ana@cefet.br", "qualquer")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meDevolveOUsuarioDoToken() throws Exception {
        when(usuarioRepository.findByEmail("ana@cefet.br")).thenReturn(Optional.of(ana("hash")));

        mvc.perform(get("/auth/me")
                        .principal(new UsernamePasswordAuthenticationToken("ana@cefet.br", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"));
    }

    @Test
    void meSemAutenticacaoDa401() throws Exception {
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }
}
