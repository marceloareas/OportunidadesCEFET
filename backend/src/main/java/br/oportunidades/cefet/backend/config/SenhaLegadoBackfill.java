package br.oportunidades.cefet.backend.config;

import br.oportunidades.cefet.backend.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Idempotente: na subida, senhas antigas em texto puro são convertidas para BCrypt.
@Component
public class SenhaLegadoBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SenhaLegadoBackfill.class);

    private final UsuarioService usuarioService;

    public SenhaLegadoBackfill(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int migrados = usuarioService.migrarSenhasLegado();
        if (migrados > 0) {
            log.info("Senha em texto puro convertida para BCrypt em {} usuário(s).", migrados);
        }
    }
}
