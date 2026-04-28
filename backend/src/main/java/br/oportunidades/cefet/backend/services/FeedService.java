package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.dto.feed.FeedPageDTO;
import br.oportunidades.cefet.backend.models.FeedItem;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;
import br.oportunidades.cefet.backend.repositories.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import br.oportunidades.cefet.backend.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import br.oportunidades.cefet.backend.repositories.PostRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import java.util.Optional;

@Service

public class FeedService {


    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    public FeedPageDTO listarFeed(int page, int size) {
        Page<FeedItem> feedPage = feedRepository.findAllByOrderByCriadoDesc(PageRequest.of(page, size));

        feedPage.getContent().forEach(item -> {
            String tipo = "OPORTUNIDADE".equalsIgnoreCase(item.getTipo()) ? "Oportunidade" : "Post";
            List<Comentario> comentarios = comentarioRepository
                    .findByTipoEntidadePaiAndIdPostAndIdComentarioPaiIsNull(tipo, item.getReferenciaId());
            
            item.setIdComentarios(comentarios.stream().map(Comentario::getId).collect(Collectors.toList()));

            String criadorId = item.getCriadorId();

            // FALLBACK E CORREÇÃO DO BANCO: 
            if (criadorId == null && item.getReferenciaId() != null) {
                if ("OPORTUNIDADE".equalsIgnoreCase(item.getTipo())) {
                    Optional<Oportunidade> op = oportunidadeRepository.findById(item.getReferenciaId());
                    if (op.isPresent()) criadorId = op.get().getProfessorId();
                } else {
                    Optional<Post> p = postRepository.findById(item.getReferenciaId());
                    if (p.isPresent()) criadorId = p.get().getCriadorId();
                }
                
                if (criadorId != null) {
                    item.setCriadorId(criadorId);
                    feedRepository.save(item);
                }
            }

            if (criadorId != null) {
                Usuario criador = usuarioService.getUsuarioById(criadorId);
                if (criador != null) {
                    item.setNomeCriador(criador.getNome());
                    item.setImagemPerfil(criador.getImagemPerfil());
                }
            }
        });

        return new FeedPageDTO(
                feedPage.getContent(),
                feedPage.getNumber(),
                feedPage.getSize(),
                feedPage.getTotalElements(),
                feedPage.getTotalPages()
        );
    }

    public void criarFeedPost(Post post, String nomeCriador) {


        String imagemPerfil = null;
        if (post.getCriadorId() != null) {
            Usuario usuario = usuarioService.getUsuarioById(post.getCriadorId());
            if (usuario != null) {
                imagemPerfil = usuario.getImagemPerfil();
            }
        }

        FeedItem item = FeedItem.builder()
            .referenciaId(post.getId())
            .tipo("POST")
            .titulo(post.getTitulo())
            .corpo(post.getCorpo())
            .criadorId(post.getCriadorId())
            .nomeCriador(nomeCriador)
            .criado(post.getCriado())
            .imagemBase64(post.getImagemBase64())
            .idLikes(post.getIdLikes())
            .imagemPerfil(imagemPerfil)
            .build();

        feedRepository.save(item);
    }

    public void atualizarFeedPost(Post post, String nomeCriador) {
        feedRepository.findByReferenciaId(post.getId()).ifPresent(item -> {
            item.setTitulo(post.getTitulo());
            item.setCorpo(post.getCorpo());
            item.setCriadorId(post.getCriadorId());
            item.setNomeCriador(nomeCriador);
            item.setImagemBase64(post.getImagemBase64());
            item.setIdLikes(post.getIdLikes());

            feedRepository.save(item);
        });
    }

    public void criarFeedOportunidade(Oportunidade op, String nomeCriador) {


        String imagemPerfil = null;
        if (op.getProfessorId() != null) {
            Usuario usuario = usuarioService.getUsuarioById(op.getProfessorId());
            if (usuario != null) {
                imagemPerfil = usuario.getImagemPerfil();
            }
        }

        FeedItem item = FeedItem.builder()
            .referenciaId(op.getId())
            .tipo("OPORTUNIDADE")
            .titulo(op.getNome())
            .corpo(op.getDescricao())
            .criadorId(op.getProfessorId())
            .nomeCriador(nomeCriador)
            .criado(op.getCriado())
            .imagemBase64(op.getImagemBase64())
            .idLikes(op.getIdLikes())
            .quantidadeDeVagas(op.getQuantidadeDeVagas())
            .vagasPreenchidas(op.getVagasPreenchidas())
            .finalizada(op.getFinalizada())
            .alunosCandidatosId(op.getAlunosCandidatosId())
            .alunosAprovadosId(op.getAlunosAprovadosId())
            .imagemPerfil(imagemPerfil)
            .build();

        feedRepository.save(item);
    }

    public void atualizarFeedOportunidade(Oportunidade op, String nomeCriador) {
        feedRepository.findByReferenciaId(op.getId()).ifPresent(item -> {
            item.setTitulo(op.getNome());
            item.setCorpo(op.getDescricao());
            item.setCriadorId(op.getProfessorId());
            item.setNomeCriador(nomeCriador);
            item.setImagemBase64(op.getImagemBase64());
            item.setIdLikes(op.getIdLikes());

            item.setQuantidadeDeVagas(op.getQuantidadeDeVagas());
            item.setVagasPreenchidas(op.getVagasPreenchidas());
            item.setFinalizada(op.getFinalizada());
            item.setAlunosCandidatosId(op.getAlunosCandidatosId());
            item.setAlunosAprovadosId(op.getAlunosAprovadosId());

            feedRepository.save(item);
        });
    }

    public void deletarFeedItem(String referenciaId) {
        feedRepository.deleteByReferenciaId(referenciaId);
    }
}