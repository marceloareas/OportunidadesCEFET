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
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    public FeedPageDTO listarFeed(int page, int size) {

        Page<FeedItem> feedPage =
                feedRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(page, size)
                );

        List<FeedResponseDTO> items = feedPage.getContent()
                .stream()
                .map(this::montarFeedItem)
                .filter(Objects::nonNull)
                .toList();

        return new FeedPageDTO(
                items,
                feedPage.getNumber(),
                feedPage.getSize(),
                feedPage.getTotalElements(),
                feedPage.getTotalPages()
        );
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

                .build();
    }
}
