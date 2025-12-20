package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Usuario; 
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(String id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario createUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(String id, Usuario usuario) {
        return usuarioRepository.findById(id).map(existing -> {
            // Atualiza apenas campos fornecidos; mantém senha e função se não vierem na requisição
            if (usuario.getNome() != null) {
                existing.setNome(usuario.getNome());
            }
            if (usuario.getEmail() != null) {
                existing.setEmail(usuario.getEmail());
            }
            if (usuario.getMatricula() != null) {
                existing.setMatricula(usuario.getMatricula());
            }
            if (usuario.getFuncao() != null) {
                existing.setFuncao(usuario.getFuncao());
            }
            if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
                existing.setSenha(usuario.getSenha());
            }
            return usuarioRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteUsuario(String id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
