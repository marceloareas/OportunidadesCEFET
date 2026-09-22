package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Notificacao;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.repositories.NotificacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
