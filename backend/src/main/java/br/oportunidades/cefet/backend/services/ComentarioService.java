package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.TipoFeed;
import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    @Autowired
    public ComentarioService(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
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
        if (comentario.getCreatedAt() == null) {
            comentario.setCreatedAt(new Date());
        }
        return comentarioRepository.save(comentario);
    }
}
