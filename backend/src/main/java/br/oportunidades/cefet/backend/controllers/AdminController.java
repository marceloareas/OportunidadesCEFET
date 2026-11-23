package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.services.AdminService;
import br.oportunidades.cefet.backend.services.PermissoesUsuario;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Sprint 6: rotas de moderação. O SecurityConfig já barra /admin/** para quem não é ADMIN;
// a checagem aqui repete a regra para não depender só da configuração.
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final PermissoesUsuario permissoes;

    public AdminController(AdminService adminService, PermissoesUsuario permissoes) {
        this.adminService = adminService;
        this.permissoes = permissoes;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<Page<Usuario>> listarUsuarios(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        permissoes.exigirAdmin(authentication);
        return ResponseEntity.ok(adminService.listarUsuarios(busca, page, size));
    }

    @PatchMapping("/usuarios/{id}/desativar")
    public ResponseEntity<Usuario> desativar(@PathVariable String id, Authentication authentication) {
        Usuario admin = permissoes.exigirAdmin(authentication);
        return ResponseEntity.ok(adminService.desativar(admin, id));
    }

    @PatchMapping("/usuarios/{id}/reativar")
    public ResponseEntity<Usuario> reativar(@PathVariable String id, Authentication authentication) {
        permissoes.exigirAdmin(authentication);
        return ResponseEntity.ok(adminService.reativar(id));
    }
}
