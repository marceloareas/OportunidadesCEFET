package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Notificacao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificacaoRepository extends MongoRepository<Notificacao, String> {
    List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(String usuarioId);
    long countByUsuarioIdAndLidaFalse(String usuarioId);
}
