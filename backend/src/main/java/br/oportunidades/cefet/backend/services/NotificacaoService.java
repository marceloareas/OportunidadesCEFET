package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Notificacao;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.repositories.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

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

    // Efeito colateral dos eventos de negócio: uma falha aqui nunca pode interromper a ação principal.
    public void notificar(String usuarioId, TipoNotificacao tipo, String mensagem, String referenciaId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            return;
        }
        try {
            criar(usuarioId, tipo, mensagem, referenciaId);
        } catch (RuntimeException e) {
            log.warn("Falha ao criar notificação {} para o usuário {}", tipo, usuarioId, e);
        }
    }

    public List<Notificacao> listarPorUsuario(String usuarioId) {
        return notificacaoRepository.findTop30ByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    public long contarNaoLidas(String usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    public void marcarComoLida(String id, String usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada."));
        if (!usuarioId.equals(notificacao.getUsuarioId())) {
            throw new SecurityException("Notificação pertence a outro usuário.");
        }
        if (!notificacao.isLida()) {
            notificacao.setLida(true);
            notificacaoRepository.save(notificacao);
        }
    }

    public void marcarTodasComoLidas(String usuarioId) {
        List<Notificacao> naoLidas = notificacaoRepository.findByUsuarioIdAndLidaFalse(usuarioId);
        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);
    }
}
