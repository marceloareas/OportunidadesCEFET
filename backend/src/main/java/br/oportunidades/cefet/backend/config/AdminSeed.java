package br.oportunidades.cefet.backend.config;

import br.oportunidades.cefet.backend.services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Sprint 6: cria o perfil administrador na subida do sistema. Idempotente: se a conta já
// existe, não duplica nem troca a senha. E-mail, senha e nome vêm do .env (ADMIN_*).
@Component
public class AdminSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeed.class);

    private final UsuarioService usuarioService;
    private final String email;
    private final String senha;
    private final String nome;

    public AdminSeed(UsuarioService usuarioService,
                     @Value("${app.admin.email}") String email,
                     @Value("${app.admin.senha}") String senha,
                     @Value("${app.admin.nome}") String nome) {
        this.usuarioService = usuarioService;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioService.garantirAdmin(email, senha, nome)) {
            log.info("Perfil administrador garantido para {}.", email);
        }
    }
}
