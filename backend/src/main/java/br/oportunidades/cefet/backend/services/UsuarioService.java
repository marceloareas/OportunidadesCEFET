package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Usuario; 
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Date;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<Usuario> getAllUsuarios(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return usuarioRepository.findAll(pageable);
    }

    public Usuario getUsuarioById(String id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario createUsuario(Usuario usuario) {
        if (usuario.getEmail() != null && usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com esse e-mail.");
        }
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        if (usuario.getCriado() == null) {
            usuario.setCriado(new Date());
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(String id, Usuario usuario) {
        return usuarioRepository.findById(id).map(existing -> {
            if (usuario.getEmail() != null) {
                usuarioRepository.findByEmail(usuario.getEmail())
                        .filter(outro -> !outro.getId().equals(id))
                        .ifPresent(outro -> {
                            throw new IllegalArgumentException("Já existe outro usuário cadastrado com esse e-mail.");
                        });
            }
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
                existing.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }
            if (usuario.getImagemPerfil() != null) {
                existing.setImagemPerfil(usuario.getImagemPerfil());
            }
            if (usuario.getLinkPortfolio() != null) {
                existing.setLinkPortfolio(usuario.getLinkPortfolio());
            }
            if (usuario.getLinkCurriculo() != null) {
                existing.setLinkCurriculo(usuario.getLinkCurriculo());
            }
            return usuarioRepository.save(existing);
        }).orElse(null);
    }

	public java.util.Optional<Usuario> findByEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

    public boolean deleteUsuario(String id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
