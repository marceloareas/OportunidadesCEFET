package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.repositories.PostRepository;

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

}
