package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Notificacao;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.repositories.NotificacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    public Notificacao criar(String usuarioId, TipoNotificacao tipo, String mensagem, String referenciaId) {
        Notificacao notificacao = Notificacao.builder()
                .usuarioId(usuarioId)
                .tipo(tipo)
                .mensagem(mensagem)
                .referenciaId(referenciaId)
                .lida(false)
                .build();
        return notificacaoRepository.save(notificacao);
    }

    public List<Notificacao> listarPorUsuario(String usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    public long contarNaoLidas(String usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }
}
