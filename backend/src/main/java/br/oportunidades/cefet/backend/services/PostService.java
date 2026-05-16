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

    private PostRepository postRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FeedService feedService;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Optional<Post> buscarPorId(String id) {
        Optional<Post> opt = postRepository.findById(id);
        opt.ifPresent(this::preencherDadosCriador);
        return opt;
    }

    public Post salvar(Post post) {
        Post salvo = postRepository.save(post);

        feedService.criarFeedPost(salvo);

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

            feedService.atualizarFeedPost(salvo);

            return "Like removido.";
        } else {
            likes.add(idUsuario);
            post.setIdLikes(likes);

            Post salvo = postRepository.save(post);

            feedService.atualizarFeedPost(salvo);

            return "Like adicionado.";
        }
    }

    public Page<Post> listarTodos(int page, int size) {
        Page<Post> posts = postRepository.findAllByOrderByCriadoDesc(PageRequest.of(page, size));
        posts.getContent().forEach(this::preencherDadosCriador);
        return posts;
    }
    
    public Page<Post> listarPorUsuario(String criadorId, int page, int size) {
        Page<Post> posts = postRepository.findAllByCriadorIdOrderByCriadoDesc(criadorId, PageRequest.of(page, size));
        posts.getContent().forEach(this::preencherDadosCriador);
        return posts;
    }

    private void preencherDadosCriador(Post post) {
        if (post.getCriadorId() != null) {
            usuarioRepository.findById(post.getCriadorId()).ifPresent(criador -> {
                post.setNomeCriador(criador.getNome());
                post.setImagemPerfil(criador.getImagemPerfil());
            });
        }
    }
}