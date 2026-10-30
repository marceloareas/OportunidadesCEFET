package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.dto.feed.FeedPageDTO;
import br.oportunidades.cefet.backend.dto.feed.FeedResponseDTO;
import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import br.oportunidades.cefet.backend.enums.StatusOportunidade;
import br.oportunidades.cefet.backend.models.Candidatura;
import br.oportunidades.cefet.backend.models.FeedItem;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.CandidaturaRepository;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;
import br.oportunidades.cefet.backend.repositories.FeedRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// Montagem do feed (M2): feed completo, item órfão e filtros de oportunidade.
@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock private UsuarioService usuarioService;
    @Mock private FeedRepository feedRepository;
    @Mock private PostRepository postRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private CandidaturaRepository candidaturaRepository;
    @Mock private ComentarioRepository comentarioRepository;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks
    private FeedService service;

    private void feedCom(FeedItem... itens) {
        when(feedRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(itens), PageRequest.of(0, 20), itens.length));
    }

    @Test
    void feedSemFiltroMontaPostComDadosDoCriador() {
        feedCom(FeedItem.builder().id("f1").referenciaId("p1").tipo("POST").likesCount(2).build());
        when(postRepository.findById("p1")).thenReturn(Optional.of(
                Post.builder().id("p1").titulo("Olá").criadorId("u1").build()));
        when(usuarioService.getUsuarioById("u1"))
                .thenReturn(Usuario.builder().id("u1").nome("Ana").imagemPerfil("ana.png").build());
        when(comentarioRepository.countByTipoEntidadePaiAndIdPost(any(), eq("p1"))).thenReturn(3L);

        FeedPageDTO pagina = service.listarFeed(0, 20, null, null, null, null);

        assertEquals(1, pagina.getContent().size());
        FeedResponseDTO post = pagina.getContent().get(0);
        assertEquals("POST", post.getTipo());
        assertEquals("Olá", post.getTitulo());
        assertEquals("Ana", post.getNomeCriador());
        assertEquals(2, post.getLikesCount());
        assertEquals(3, post.getComentariosCount());
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void itemCujoPostFoiApagadoNaoApareceNoFeed() {
        feedCom(
                FeedItem.builder().id("f1").referenciaId("apagado").tipo("POST").build(),
                FeedItem.builder().id("f2").referenciaId("p2").tipo("POST").build());
        when(postRepository.findById("apagado")).thenReturn(Optional.empty());
        when(postRepository.findById("p2")).thenReturn(Optional.of(
                Post.builder().id("p2").titulo("Existe").criadorId("u1").build()));

        FeedPageDTO pagina = service.listarFeed(0, 20, "", " ", null, null);

        assertEquals(1, pagina.getContent().size());
        assertNotNull(pagina.getContent().get(0));
        assertEquals("p2", pagina.getContent().get(0).getId());
    }

    @Test
    void oportunidadeTrazStatusDaCandidaturaDoUsuario() {
        feedCom(FeedItem.builder().id("f1").referenciaId("op-1").tipo("OPORTUNIDADE").build());
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(
                Oportunidade.builder().id("op-1").nome("Monitoria").professorId("prof-1")
                        .quantidadeDeVagas(2).vagasPreenchidas(1).build()));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(
                Candidatura.builder().alunoId("aluno-1").oportunidadeId("op-1").status(StatusCandidatura.APROVADO).build(),
                Candidatura.builder().alunoId("aluno-2").oportunidadeId("op-1").status(StatusCandidatura.CONCORRENDO).build()));

        FeedResponseDTO op = service.listarFeed(0, 20, null, null, null, "aluno-1").getContent().get(0);

        assertEquals("OPORTUNIDADE", op.getTipo());
        assertEquals(StatusOportunidade.INSCRICOES_ABERTAS, op.getStatus());
        assertEquals(List.of("aluno-1", "aluno-2"), op.getAlunosCandidatosId());
        assertEquals(List.of("aluno-1"), op.getAlunosAprovadosId());
        assertEquals(StatusCandidatura.APROVADO, op.getStatusCandidaturaAluno());
    }

    @Test
    void semUsuarioNaoHaStatusDeCandidatura() {
        feedCom(FeedItem.builder().id("f1").referenciaId("op-1").tipo("OPORTUNIDADE").build());
        when(oportunidadeRepository.findById("op-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").build()));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(
                Candidatura.builder().alunoId("aluno-1").status(StatusCandidatura.APROVADO).build()));

        assertNull(service.listarFeed(0, 20, null, null, null, null).getContent().get(0).getStatusCandidaturaAluno());
    }

    @Test
    void comFiltroConsultaSoOportunidadesEPagina() {
        when(mongoTemplate.count(any(Query.class), eq(Oportunidade.class))).thenReturn(25L);
        when(mongoTemplate.find(any(Query.class), eq(Oportunidade.class)))
                .thenReturn(List.of(Oportunidade.builder().id("op-1").build()));
        when(feedRepository.findByReferenciaIdIn(List.of("op-1"))).thenReturn(List.of(
                FeedItem.builder().id("f1").referenciaId("op-1").tipo("OPORTUNIDADE").build()));
        when(oportunidadeRepository.findById("op-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").idCategoria("PESQUISA").build()));

        FeedPageDTO pagina = service.listarFeed(1, 10, null, "pesquisa", null, null);

        assertEquals(25, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
        assertEquals(1, pagina.getPage());
        assertEquals("PESQUISA", pagina.getContent().get(0).getIdCategoria());
        verify(feedRepository, never()).findAllByOrderByCreatedAtDesc(any());
    }

    // B1: busca por texto livre
    private static String padraoDe(Object valor) {
        if (valor instanceof java.util.regex.Pattern p) return p.pattern();
        if (valor instanceof org.bson.BsonRegularExpression r) return r.getPattern();
        return String.valueOf(valor);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buscaProcuraNoNomeENaDescricaoComTextoLiteral() {
        org.bson.Document filtro = FeedService.criteriaParaTexto("  C++ (IA) ").getCriteriaObject();
        List<org.bson.Document> alternativas = (List<org.bson.Document>) filtro.get("$or");

        assertEquals(2, alternativas.size());
        String padraoNome = padraoDe(alternativas.get(0).get("nome"));
        assertEquals(padraoNome, padraoDe(alternativas.get(1).get("descricao")));

        java.util.regex.Pattern regex =
                java.util.regex.Pattern.compile(padraoNome, java.util.regex.Pattern.CASE_INSENSITIVE);
        assertTrue(regex.matcher("Monitoria de c++ (ia) aplicada").find());
        assertFalse(regex.matcher("Monitoria de C (IA)").find());
    }

    @Test
    void buscaUsaAConsultaDeOportunidadesESomaComOsFiltros() {
        when(mongoTemplate.count(any(Query.class), eq(Oportunidade.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Oportunidade.class))).thenReturn(List.of());
        when(feedRepository.findByReferenciaIdIn(List.of())).thenReturn(List.of());

        service.listarFeed(0, 10, null, "PESQUISA", null, "robótica", null);

        org.mockito.ArgumentCaptor<Query> captor = org.mockito.ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Oportunidade.class));
        String consulta = captor.getValue().getQueryObject().toJson();
        assertTrue(consulta.contains("robótica"), consulta);
        assertTrue(consulta.contains("PESQUISA"), consulta);
        verify(feedRepository, never()).findAllByOrderByCreatedAtDesc(any());
    }

    @Test
    void buscaEmBrancoMostraOFeedCompleto() {
        feedCom();

        service.listarFeed(0, 20, null, null, null, "   ", null);

        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void criarOportunidadeGeraItemNoFeed() {
        Oportunidade op = Oportunidade.builder().id("op-1").build();

        service.criarFeedOportunidade(op);

        verify(feedRepository).save(argThat(item ->
                "op-1".equals(item.getReferenciaId()) && "OPORTUNIDADE".equals(item.getTipo())));
    }
}
