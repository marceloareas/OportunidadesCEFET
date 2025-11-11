package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.repositories.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService{

    @Autowired
    private PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> listarTodos() { return postRepository.findAll(); }
    public Optional<Post> buscarPorId(String id) { return postRepository.findById(id); }
    public Post salvar(Post post) { return postRepository.save(post); }
    public void deletar(String id) { postRepository.deleteById(id); }

    public String alternarLike(String idPost, String idUsuario) {
        Optional<Post> opt = postRepository.findById(idPost);
        if (opt.isEmpty()) return "Post não encontrado.";

        Post post = opt.get();
        List<String> likes = post.getIdLikes();
        
        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(idUsuario)) {
            likes.remove(idUsuario);
            post.setIdLikes(likes);
            postRepository.save(post);
            return "Like removido.";
        } else {
            likes.add(idUsuario);
            post.setIdLikes(likes);
            postRepository.save(post);
            return "Like adicionado.";
        }
    }
}
