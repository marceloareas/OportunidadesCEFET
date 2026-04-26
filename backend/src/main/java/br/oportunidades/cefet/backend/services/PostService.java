package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.PostRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FeedService feedService;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> listarTodos(int page, int size) {
        return postRepository.findAllByOrderByCriadoDesc(PageRequest.of(page, size));
    }

    public Optional<Post> buscarPorId(String id) {
        return postRepository.findById(id);
    }

    public Post salvar(Post post) {
        Post salvo = postRepository.save(post);

        String nomeCriador = usuarioRepository.findById(salvo.getCriadorId())
                .map(Usuario::getNome)
                .orElse("Usuário Anônimo");

        feedService.criarFeedPost(salvo, nomeCriador);

        return salvo;
    }

    public void deletar(String id) {
        postRepository.deleteById(id);
        feedService.deletarFeedItem(id);
    }

    public String alternarLike(String idPost, String idUsuario) {
        Optional<Post> opt = postRepository.findById(idPost);
        if (opt.isEmpty()) return "Post não encontrado.";

        Post post = opt.get();
        List<String> likes = post.getIdLikes();

        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(idUsuario)) {
            likes.remove(idUsuario);
            post.setIdLikes(likes);

            Post salvo = postRepository.save(post);

            String nomeCriador = usuarioRepository.findById(salvo.getCriadorId())
                    .map(Usuario::getNome)
                    .orElse("Usuário Anônimo");

            feedService.atualizarFeedPost(salvo, nomeCriador);

            return "Like removido.";
        } else {
            likes.add(idUsuario);
            post.setIdLikes(likes);

            Post salvo = postRepository.save(post);

            String nomeCriador = usuarioRepository.findById(salvo.getCriadorId())
                    .map(Usuario::getNome)
                    .orElse("Usuário Anônimo");

            feedService.atualizarFeedPost(salvo, nomeCriador);

            return "Like adicionado.";
        }
    }

    public Page<Post> listarPorUsuario(String criadorId, int page, int size) {
        return postRepository.findAllByCriadorIdOrderByCriadoDesc(
                criadorId,
                PageRequest.of(page, size)
        );
    }
}