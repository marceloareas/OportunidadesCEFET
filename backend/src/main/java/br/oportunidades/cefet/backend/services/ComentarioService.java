package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComentarioService {

    @Autowired
    private final ComentarioRepository comentarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository){
        this.comentarioRepository = comentarioRepository;
    }

    public List<Comentario> listarTodos() { return comentarioRepository.findAll(); }
    public Optional<Comentario> buscarPorId(String id) { return comentarioRepository.findById(id); }
    public void deletar(String id) { comentarioRepository.deleteById(id); }

    public Comentario salvar(Comentario comentario) {

        if (comentario.getTexto() == null || comentario.getTexto().isBlank()) {
            throw new IllegalArgumentException("Texto do comentário é obrigatório.");
        }

        if (comentario.getTipoEntidadePai() == null ||
                comentario.getIdPost() == null) {
            throw new IllegalArgumentException("Comentário precisa de entidade pai.");
        }

        return comentarioRepository.save(comentario);
    }

    public List<Comentario> listarPorEntidade(String tipo, String id) {
        return comentarioRepository.findByTipoEntidadePaiAndIdPost(tipo, id);
    }


}
