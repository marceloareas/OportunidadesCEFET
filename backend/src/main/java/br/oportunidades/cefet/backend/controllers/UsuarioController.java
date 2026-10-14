package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/users")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    //Listar todos
    @GetMapping
    public ResponseEntity<Page<Usuario>> getAllUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(usuarioService.getAllUsuarios(page, size));
    }

    //Busca por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable String id) {
        Usuario usuario = usuarioService.getUsuarioById(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Criar novo
    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.createUsuario(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable String id, @RequestBody Usuario usuario){
        Usuario existingUsuario = usuarioService.getUsuarioById(id);
        if (existingUsuario != null) {
            Usuario updatedUsuario = usuarioService.updateUsuario(id, usuario);
            return ResponseEntity.ok(updatedUsuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String id) {
        Usuario existingUsuario = usuarioService.getUsuarioById(id);
        if (existingUsuario != null) {
            usuarioService.deleteUsuario(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/imagem")
    public ResponseEntity<Usuario> removerImagemPerfil(@PathVariable String id, Authentication authentication) {
        Usuario usuario = usuarioService.getUsuarioById(id);
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
        if (authentication == null || !authentication.getName().equals(usuario.getEmail())) {
            throw new SecurityException("Você só pode remover a sua própria foto.");
        }
        return ResponseEntity.ok(usuarioService.removerImagemPerfil(id));
    }

}
