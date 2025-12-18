package br.oportunidades.cefet.backend.services;

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
                .findByTipoEntidadePaiAndIdPostAndIdComentarioPaiIsNull(
                        "Post",
                        idPost
                );
    }

    public List<Comentario> listarComentariosDeOportunidade(String idOportunidade) {
        return comentarioRepository
                .findByTipoEntidadePaiAndIdPostAndIdComentarioPaiIsNull(
                        "Oportunidade",
                        idOportunidade
                );
    }

    public List<Comentario> listarRespostas(String idComentarioPai) {
        return comentarioRepository.findByIdComentarioPai(idComentarioPai);
    }

    public Comentario salvar(Comentario comentario) {
        if (comentario.getDataComentario() == null) {
            comentario.setDataComentario(new Date());
        }
        return comentarioRepository.save(comentario);
    }
}
