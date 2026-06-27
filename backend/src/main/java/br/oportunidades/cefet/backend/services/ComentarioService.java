package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.TipoFeed;
import br.oportunidades.cefet.backend.enums.StatusOportunidade;
import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.repositories.ComentarioRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final OportunidadeRepository oportunidadeRepository;

    @Autowired
    public ComentarioService(ComentarioRepository comentarioRepository, OportunidadeRepository oportunidadeRepository) {
        this.comentarioRepository = comentarioRepository;
        this.oportunidadeRepository = oportunidadeRepository;
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
        if (comentario.getTipoEntidadePai() == TipoFeed.OPORTUNIDADE && comentario.getIdPost() != null) {
            oportunidadeRepository.findById(comentario.getIdPost()).ifPresent(oportunidade -> {
                if (OportunidadeStatusHelper.calcularStatus(oportunidade) == StatusOportunidade.FINALIZADA) {
                    throw new IllegalStateException("Oportunidade finalizada. Não é possível enviar mensagens.");
                }
            });
        }

        if (comentario.getCreatedAt() == null) {
            comentario.setCreatedAt(new Date());
        }
        return comentarioRepository.save(comentario);
    }
}
