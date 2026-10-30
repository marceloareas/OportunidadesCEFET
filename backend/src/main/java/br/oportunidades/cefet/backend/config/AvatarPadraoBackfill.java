package br.oportunidades.cefet.backend.config;

import br.oportunidades.cefet.backend.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Idempotente: na subida, usuários antigos sem foto recebem a imagem padrão do seu perfil.
@Component
public class AvatarPadraoBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AvatarPadraoBackfill.class);

    private final UsuarioService usuarioService;

    public AvatarPadraoBackfill(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int atualizados = usuarioService.preencherImagensPadrao();
        if (atualizados > 0) {
            log.info("Imagem de perfil padrão aplicada a {} usuário(s) sem foto.", atualizados);
        }
    }
}
