package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.enums.TipoFeed;
import br.oportunidades.cefet.backend.models.Comentario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends MongoRepository<Comentario, String> {

    List<Comentario> findByTipoEntidadePaiAndIdPost(
            TipoFeed tipoEntidadePai,
            String idPost
    );
}
