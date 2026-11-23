package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Sprint 6: apagar uma postagem não deixa comentários, salvos e candidaturas órfãos.
@ExtendWith(MockitoExtension.class)
class ModeracaoCascataTest {

    @Mock private FeedRepository feedRepository;
    @Mock private ComentarioRepository comentarioRepository;
    @Mock private SavedItemRepository savedItemRepository;

    @InjectMocks
    private FeedService feedService;

    @Test
    void tirarDoFeedApagaComentariosESalvos() {
        feedService.deletarFeedItem("p1");

        verify(feedRepository).deleteByReferenciaId("p1");
        verify(comentarioRepository).deleteByIdPost("p1");
        verify(savedItemRepository).deleteByFeedItemId("p1");
    }

    @Test
    void apagarOportunidadeApagaCandidaturas() {
        OportunidadeService service = new OportunidadeService();
        OportunidadeRepository oportunidadeRepository = mock(OportunidadeRepository.class);
        CandidaturaRepository candidaturaRepository = mock(CandidaturaRepository.class);
        FeedService feed = mock(FeedService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "oportunidadeRepository", oportunidadeRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "candidaturaRepository", candidaturaRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "feedService", feed);

        service.deletar("op-1");

        verify(oportunidadeRepository).deleteById("op-1");
        verify(candidaturaRepository).deleteByOportunidadeId("op-1");
        verify(feed).deletarFeedItem("op-1");
    }

    @Test
    void apagarComentarioExistente() {
        ComentarioService service = new ComentarioService(comentarioRepository, null, null, null, null);
        when(comentarioRepository.findById("c1")).thenReturn(Optional.of(Comentario.builder().id("c1").build()));

        service.deletar("c1");

        verify(comentarioRepository).deleteById("c1");
    }

    @Test
    void apagarComentarioInexistenteLanca404() {
        ComentarioService service = new ComentarioService(comentarioRepository, null, null, null, null);
        when(comentarioRepository.findById("x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletar("x"));
        verify(comentarioRepository, never()).deleteById(anyString());
    }
}
