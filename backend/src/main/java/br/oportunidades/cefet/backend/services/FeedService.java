package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.dto.feed.FeedPageDTO;
import br.oportunidades.cefet.backend.dto.feed.FeedResponseDTO;
import br.oportunidades.cefet.backend.models.FeedItem;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.repositories.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import br.oportunidades.cefet.backend.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.oportunidades.cefet.backend.repositories.PostRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.CandidaturaRepository;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;
import br.oportunidades.cefet.backend.models.Candidatura;
import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import br.oportunidades.cefet.backend.enums.TipoFeed;
import java.util.Optional;

@Service

public class FeedService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    @Autowired
    private CandidaturaRepository candidaturaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public FeedPageDTO listarFeed(int page, int size, String status, String categoria, String area, String userId) {

        boolean temFiltro =
                (status != null && !status.isBlank())
                        || (categoria != null && !categoria.isBlank())
                        || (area != null && !area.isBlank());

        // Sem filtro: feed completo (posts + oportunidades), como antes.
        if (!temFiltro) {
            return listarFeedCompleto(page, size, userId);
        }

        // Com filtro: os campos filtráveis (status/categoria/área) só existem em
        // Oportunidade, então consultamos essa coleção diretamente (posts ficam de fora).
        return listarOportunidadesFiltradas(page, size, status, categoria, area, userId);
    }

    private FeedPageDTO listarFeedCompleto(int page, int size, String userId) {

        Page<FeedItem> feedPage =
                feedRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(page, size)
                );

        List<FeedResponseDTO> items = feedPage.getContent()
                .stream()
                .map(item -> montarFeedItem(item, userId))
                .toList();

        return new FeedPageDTO(
                items,
                feedPage.getNumber(),
                feedPage.getSize(),
                feedPage.getTotalElements(),
                feedPage.getTotalPages()
        );
    }

    private FeedPageDTO listarOportunidadesFiltradas(
            int page, int size, String status, String categoria, String area, String userId) {

        List<Criteria> condicoes = new ArrayList<>();

        if (categoria != null && !categoria.isBlank()) {
            condicoes.add(Criteria.where("idCategoria").is(categoria.trim().toUpperCase()));
        }

        if (area != null && !area.isBlank()) {
            // grandesAreas é uma lista; $in casa quando a lista contém o valor.
            condicoes.add(Criteria.where("grandesAreas").in(area.trim().toUpperCase()));
        }

        if (status != null && !status.isBlank()) {
            condicoes.add(criteriaParaStatus(status.trim().toUpperCase()));
        }

        Query baseQuery = new Query();
        if (!condicoes.isEmpty()) {
            baseQuery.addCriteria(new Criteria().andOperator(condicoes.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(baseQuery, Oportunidade.class);

        Query pageQuery = Query.of(baseQuery)
                .with(Sort.by(Sort.Direction.DESC, "criado"))
                .skip((long) page * size)
                .limit(size);

        List<Oportunidade> oportunidades = mongoTemplate.find(pageQuery, Oportunidade.class);

        // Recupera o FeedItem correspondente (id do documento de feed, likes, createdAt).
        List<String> ids = oportunidades.stream().map(Oportunidade::getId).toList();
        Map<String, FeedItem> feedPorReferencia = feedRepository.findByReferenciaIdIn(ids)
                .stream()
                .collect(Collectors.toMap(
                        FeedItem::getReferenciaId,
                        Function.identity(),
                        (a, b) -> a
                ));

        List<FeedResponseDTO> items = oportunidades.stream()
                .map(op -> {
                    FeedItem feedItem = feedPorReferencia.get(op.getId());
                    return feedItem != null ? montarFeedItem(feedItem, userId) : null;
                })
                .filter(Objects::nonNull)
                .toList();

        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        return new FeedPageDTO(items, page, size, total, totalPages);
    }

    // Traduz o status (calculado dinamicamente a partir das datas e do flag
    // 'finalizada') em condições sobre os campos persistidos da Oportunidade.
    private Criteria criteriaParaStatus(String status) {

        Date agora = new Date();
        Criteria naoFinalizada = Criteria.where("finalizada").ne(true);

        switch (status) {
            case "FINALIZADA":
                return Criteria.where("finalizada").is(true);

            case "INSCRICOES_EM_BREVE":
                // ainda não começou: dataInicioInscricao > agora (gt já exclui null)
                return new Criteria().andOperator(
                        naoFinalizada,
                        Criteria.where("dataInicioInscricao").gt(agora)
                );

            case "INSCRICOES_ENCERRADAS":
                // período acabou: dataFimInscricao < agora (lt já exclui null)
                return new Criteria().andOperator(
                        naoFinalizada,
                        Criteria.where("dataFimInscricao").lt(agora)
                );

            case "INSCRICOES_ABERTAS":
            default:
                // não finalizada, já começou (ou sem início) e ainda não terminou (ou sem fim)
                return new Criteria().andOperator(
                        naoFinalizada,
                        new Criteria().orOperator(
                                Criteria.where("dataInicioInscricao").is(null),
                                Criteria.where("dataInicioInscricao").lte(agora)
                        ),
                        new Criteria().orOperator(
                                Criteria.where("dataFimInscricao").is(null),
                                Criteria.where("dataFimInscricao").gte(agora)
                        )
                );
        }
    }

    public void criarFeedPost(Post post) {

        FeedItem item = FeedItem.builder()
                .referenciaId(post.getId())
                .tipo("POST")
                .likesCount(post.getIdLikes().size())
                .comentariosCount(0)
                .createdAt(post.getCriado() != null ? post.getCriado() : new Date())
                .build();

        feedRepository.save(item);
    }

    public void atualizarFeedPost(Post post) {

        feedRepository.findByReferenciaId(post.getId())
                .ifPresent(item -> {

                    item.setLikesCount(post.getIdLikes().size());

                    feedRepository.save(item);
                });
    }

    public void criarFeedOportunidade(Oportunidade op) {

        FeedItem item = FeedItem.builder()
                .referenciaId(op.getId())
                .tipo("OPORTUNIDADE")
                .likesCount(op.getIdLikes().size())
                .comentariosCount(0)
                .createdAt(op.getCriado())
                .build();

        feedRepository.save(item);
    }

    public void atualizarFeedOportunidade(Oportunidade op) {

        feedRepository.findByReferenciaId(op.getId())
                .ifPresent(item -> {

                    item.setLikesCount(op.getIdLikes().size());

                    feedRepository.save(item);
                });
    }

    public void deletarFeedItem(String referenciaId) {
        feedRepository.deleteByReferenciaId(referenciaId);
    }

    FeedResponseDTO montarFeedItem(FeedItem item) {
        return montarFeedItem(item, null);
    }

    FeedResponseDTO montarFeedItem(FeedItem item, String userId) {

        if ("POST".equalsIgnoreCase(item.getTipo())) {

            Optional<Post> opt = postRepository.findById(item.getReferenciaId());

            if (opt.isEmpty()) return null;

            Post post = opt.get();

            Usuario criador = usuarioService.getUsuarioById(post.getCriadorId());

            return FeedResponseDTO.builder()
                    .id(post.getId())
                    .referenciaId(item.getReferenciaId())

                    .tipo("POST")

                    .criadorId(post.getCriadorId())

                    .titulo(post.getTitulo())
                    .corpo(post.getCorpo())

                    .imagemBase64(post.getImagemBase64())

                    .likesCount(item.getLikesCount())
                    .comentariosCount(
                            (int) comentarioRepository.countByTipoEntidadePaiAndIdPost(
                                    TipoFeed.POST, post.getId()
                            )
                    )

                    .idLikes(post.getIdLikes())

                    .createdAt(item.getCreatedAt())

                    .nomeCriador(
                            criador != null ? criador.getNome() : null
                    )

                    .imagemPerfil(
                            criador != null ? criador.getImagemPerfil() : null
                    )

                    .build();
        }

        Optional<Oportunidade> opt =
                oportunidadeRepository.findById(item.getReferenciaId());

        if (opt.isEmpty()) return null;

        Oportunidade op = opt.get();

        Usuario criador = usuarioService.getUsuarioById(op.getProfessorId());

        OportunidadeStatusHelper.aplicarStatus(op);

        List<Candidatura> candidaturas =
                candidaturaRepository.findByOportunidadeId(op.getId());

        List<String> alunosCandidatosId =
                candidaturas.stream().map(Candidatura::getAlunoId).toList();

        List<String> alunosAprovadosId = candidaturas.stream()
                .filter(c -> c.getStatus() == StatusCandidatura.APROVADO)
                .map(Candidatura::getAlunoId)
                .toList();

        StatusCandidatura statusCandidaturaAluno =
                userId == null || userId.isBlank()
                        ? null
                        : candidaturas.stream()
                                .filter(c -> userId.equals(c.getAlunoId()))
                                .map(Candidatura::getStatus)
                                .findFirst()
                                .orElse(null);

        return FeedResponseDTO.builder()
                .id(item.getId())
                .referenciaId(item.getReferenciaId())

                .tipo("OPORTUNIDADE")

                .criadorId(op.getProfessorId())

                .titulo(op.getNome())
                .corpo(op.getDescricao())

                .imagemBase64(op.getImagemBase64())

                .dataInicioInscricao(op.getDataInicioInscricao())
                .dataFimInscricao(op.getDataFimInscricao())
                .status(op.getStatus())
                .idCategoria(op.getIdCategoria())
                .grandesAreas(
                        op.getGrandesAreas() == null
                                ? java.util.Collections.emptyList()
                                : op.getGrandesAreas().stream().map(Enum::name).toList()
                )

                .likesCount(item.getLikesCount())
                .comentariosCount(
                        (int) comentarioRepository.countByTipoEntidadePaiAndIdPost(
                                TipoFeed.OPORTUNIDADE, op.getId()
                        )
                )

                .idLikes(op.getIdLikes())

                .createdAt(item.getCreatedAt())

                .nomeCriador(
                        criador != null ? criador.getNome() : null
                )

                .imagemPerfil(
                        criador != null ? criador.getImagemPerfil() : null
                )

                .quantidadeDeVagas(op.getQuantidadeDeVagas())
                .vagasPreenchidas(op.getVagasPreenchidas())
                .finalizada(op.getFinalizada())

                .alunosCandidatosId(alunosCandidatosId)
                .alunosAprovadosId(alunosAprovadosId)
                .statusCandidaturaAluno(statusCandidaturaAluno)

                .build();
    }
}
