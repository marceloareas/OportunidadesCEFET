package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.TipoFeed;
import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.PostRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceNotificacaoTest {

    @Mock private ComentarioRepository comentarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private PostRepository postRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacaoService notificacaoService;

    private ComentarioService service;

    @BeforeEach
    void setup() {
        service = new ComentarioService(comentarioRepository, oportunidadeRepository,
                postRepository, usuarioRepository, notificacaoService);
    }

    @Test
    void comentarioEmPostNotificaOAutor() {
        Comentario c = Comentario.builder().usuarioId("u-2").tipoEntidadePai(TipoFeed.POST)
                .idPost("post-1").texto("Legal!").build();
        when(postRepository.findById("post-1"))
                .thenReturn(Optional.of(Post.builder().id("post-1").criadorId("u-1").build()));
        when(usuarioRepository.findById("u-2"))
                .thenReturn(Optional.of(Usuario.builder().id("u-2").nome("Caio").build()));

        service.salvar(c);

        verify(notificacaoService).notificar(
                "u-1", TipoNotificacao.COMENTARIO_RECEBIDO, "Caio comentou na sua publicação.", "post-1");
    }

    @Test
    void autorComentandoNoProprioPostNaoNotifica() {
        Comentario c = Comentario.builder().usuarioId("u-1").tipoEntidadePai(TipoFeed.POST)
                .idPost("post-1").texto("Atualizando").build();
        when(postRepository.findById("post-1"))
                .thenReturn(Optional.of(Post.builder().id("post-1").criadorId("u-1").build()));

        service.salvar(c);

        verifyNoInteractions(notificacaoService);
    }

    @Test
    void comentarioEmOportunidadeNotificaOProfessor() {
        Comentario c = Comentario.builder().usuarioId("aluno-1").tipoEntidadePai(TipoFeed.OPORTUNIDADE)
                .idPost("op-1").texto("Tenho interesse").build();
        Oportunidade op = Oportunidade.builder().id("op-1").nome("Monitoria de Cálculo")
                .professorId("prof-1").finalizada(false).build();
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(op));
        when(usuarioRepository.findById("aluno-1"))
                .thenReturn(Optional.of(Usuario.builder().id("aluno-1").nome("Bia").build()));

        service.salvar(c);

        verify(notificacaoService).notificar(
                "prof-1",
                TipoNotificacao.COMENTARIO_RECEBIDO,
                "Bia comentou na sua oportunidade \"Monitoria de Cálculo\".",
                "op-1");
    }
}
