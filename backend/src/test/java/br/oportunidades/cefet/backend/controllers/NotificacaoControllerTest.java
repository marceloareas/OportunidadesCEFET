package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Notificacao;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificacaoControllerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private MockMvc mvc;

    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken("ana@cefet.br", null, List.of());

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(new NotificacaoController(notificacaoService, usuarioRepository))
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
        Usuario ana = Usuario.builder().id("user-1").email("ana@cefet.br").build();
        when(usuarioRepository.findByEmail("ana@cefet.br")).thenReturn(Optional.of(ana));
    }

    @Test
    void listarRetornaNotificacoesDoUsuarioDoToken() throws Exception {
        Notificacao n = Notificacao.builder().id("n1").usuarioId("user-1")
                .tipo(TipoNotificacao.COMENTARIO_RECEBIDO).mensagem("Oi").build();
        when(notificacaoService.listarPorUsuario("user-1")).thenReturn(List.of(n));

        mvc.perform(get("/notificacoes").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("n1"))
                .andExpect(jsonPath("$[0].mensagem").value("Oi"));
    }

    @Test
    void contagemRetornaTotal() throws Exception {
        when(notificacaoService.contarNaoLidas("user-1")).thenReturn(3L);

        mvc.perform(get("/notificacoes/nao-lidas/contagem").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void marcarComoLidaRetorna204() throws Exception {
        mvc.perform(patch("/notificacoes/n1/lida").principal(auth))
                .andExpect(status().isNoContent());

        verify(notificacaoService).marcarComoLida("n1", "user-1");
    }

    @Test
    void marcarComoLidaDeOutroUsuarioRetorna403() throws Exception {
        doThrow(new SecurityException("Notificação pertence a outro usuário."))
                .when(notificacaoService).marcarComoLida("n1", "user-1");

        mvc.perform(patch("/notificacoes/n1/lida").principal(auth))
                .andExpect(status().isForbidden());
    }

    @Test
    void marcarTodasComoLidasRetorna204() throws Exception {
        mvc.perform(patch("/notificacoes/lidas").principal(auth))
                .andExpect(status().isNoContent());

        verify(notificacaoService).marcarTodasComoLidas("user-1");
    }
}
