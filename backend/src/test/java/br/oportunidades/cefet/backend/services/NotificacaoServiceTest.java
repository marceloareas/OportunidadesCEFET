package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Notificacao;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.repositories.NotificacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository repository;

    @InjectMocks
    private NotificacaoService service;

    @Test
    void criarSalvaNotificacaoNaoLidaComOsDadosInformados() {
        when(repository.save(any(Notificacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacao resultado = service.criar("user-1", TipoNotificacao.CANDIDATURA_RECEBIDA, "Nova candidatura recebida", "op-1");

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(repository).save(captor.capture());

        assertEquals("user-1", captor.getValue().getUsuarioId());
        assertEquals(TipoNotificacao.CANDIDATURA_RECEBIDA, captor.getValue().getTipo());
        assertEquals("op-1", captor.getValue().getReferenciaId());
        assertFalse(captor.getValue().isLida());
        assertSame(resultado, captor.getValue());
    }

    @Test
    void listarPorUsuarioRetornaAs30MaisRecentes() {
        List<Notificacao> lista = List.of(Notificacao.builder().id("n1").usuarioId("user-1").build());
        when(repository.findTop30ByUsuarioIdOrderByCriadoEmDesc("user-1")).thenReturn(lista);

        assertSame(lista, service.listarPorUsuario("user-1"));
    }

    @Test
    void marcarComoLidaMarcaESalva() {
        Notificacao n = Notificacao.builder().id("n1").usuarioId("user-1").lida(false).build();
        when(repository.findById("n1")).thenReturn(Optional.of(n));

        service.marcarComoLida("n1", "user-1");

        assertTrue(n.isLida());
        verify(repository).save(n);
    }

    @Test
    void marcarComoLidaInexistenteLancaNotFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.marcarComoLida("x", "user-1"));
    }

    @Test
    void marcarComoLidaDeOutroUsuarioLancaSecurity() {
        Notificacao n = Notificacao.builder().id("n1").usuarioId("user-2").lida(false).build();
        when(repository.findById("n1")).thenReturn(Optional.of(n));

        assertThrows(SecurityException.class, () -> service.marcarComoLida("n1", "user-1"));
        verify(repository, never()).save(any());
    }

    @Test
    void marcarComoLidaJaLidaNaoSalvaDeNovo() {
        Notificacao n = Notificacao.builder().id("n1").usuarioId("user-1").lida(true).build();
        when(repository.findById("n1")).thenReturn(Optional.of(n));

        service.marcarComoLida("n1", "user-1");

        verify(repository, never()).save(any());
    }

    @Test
    void marcarTodasComoLidasMarcaTodasAsNaoLidas() {
        Notificacao a = Notificacao.builder().id("a").usuarioId("user-1").lida(false).build();
        Notificacao b = Notificacao.builder().id("b").usuarioId("user-1").lida(false).build();
        List<Notificacao> naoLidas = List.of(a, b);
        when(repository.findByUsuarioIdAndLidaFalse("user-1")).thenReturn(naoLidas);

        service.marcarTodasComoLidas("user-1");

        assertTrue(a.isLida());
        assertTrue(b.isLida());
        verify(repository).saveAll(naoLidas);
    }

    @Test
    void notificarIgnoraDestinatarioNuloOuEmBranco() {
        service.notificar(null, TipoNotificacao.COMENTARIO_RECEBIDO, "msg", "ref");
        service.notificar("   ", TipoNotificacao.COMENTARIO_RECEBIDO, "msg", "ref");

        verifyNoInteractions(repository);
    }

    @Test
    void notificarNaoPropagaFalhaDoRepositorio() {
        when(repository.save(any(Notificacao.class))).thenThrow(new RuntimeException("mongo fora do ar"));

        assertDoesNotThrow(() -> service.notificar("user-1", TipoNotificacao.CANDIDATURA_APROVADA, "msg", "op-1"));
    }
}
