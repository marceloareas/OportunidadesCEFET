package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.StatusOportunidade;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    @Autowired
    public ComentarioService(ComentarioRepository comentarioRepository,
                             OportunidadeRepository oportunidadeRepository,
                             PostRepository postRepository,
                             UsuarioRepository usuarioRepository,
                             NotificacaoService notificacaoService) {
        this.comentarioRepository = comentarioRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.postRepository = postRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    public List<Comentario> listarComentariosDePost(String idPost) {
        return comentarioRepository
                .findByTipoEntidadePaiAndIdPost(
                        TipoFeed.POST,
                        idPost
                );
    }

    public List<Comentario> listarComentariosDeOportunidade(String idOportunidade) {
        return comentarioRepository
                .findByTipoEntidadePaiAndIdPost(
                        TipoFeed.OPORTUNIDADE,
                        idOportunidade
                );
    }

    public Comentario salvar(Comentario comentario) {
        Optional<Oportunidade> oportunidade = Optional.empty();
        if (comentario.getTipoEntidadePai() == TipoFeed.OPORTUNIDADE && comentario.getIdPost() != null) {
            oportunidade = oportunidadeRepository.findById(comentario.getIdPost());
            oportunidade.ifPresent(op -> {
                if (OportunidadeStatusHelper.calcularStatus(op) == StatusOportunidade.FINALIZADA) {
                    throw new IllegalStateException("Oportunidade finalizada. Não é possível enviar mensagens.");
                }
            });
        }

        if (comentario.getCreatedAt() == null) {
            comentario.setCreatedAt(new Date());
        }
        Comentario salvo = comentarioRepository.save(comentario);

        notificarAutor(comentario, oportunidade);

        return salvo;
    }

    private void notificarAutor(Comentario comentario, Optional<Oportunidade> oportunidade) {
        String autorId;
        String acao;
        if (comentario.getTipoEntidadePai() == TipoFeed.OPORTUNIDADE) {
            if (oportunidade.isEmpty()) {
                return;
            }
            autorId = oportunidade.get().getProfessorId();
            acao = "comentou na sua oportunidade \"" + oportunidade.get().getNome() + "\".";
        } else {
            if (comentario.getIdPost() == null) {
                return;
            }
            autorId = postRepository.findById(comentario.getIdPost()).map(Post::getCriadorId).orElse(null);
            acao = "comentou na sua publicação.";
        }

        if (autorId == null || autorId.equals(comentario.getUsuarioId())) {
            return;
        }

        String nome = Optional.ofNullable(comentario.getUsuarioId())
                .flatMap(usuarioRepository::findById)
                .map(Usuario::getNome)
                .filter(n -> !n.isBlank())
                .orElse("Alguém");

        notificacaoService.notificar(autorId, TipoNotificacao.COMENTARIO_RECEBIDO, nome + " " + acao, comentario.getIdPost());
    }
}
