package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ConflictException;
import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
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

    public static final String AVATAR_ALUNO = "avatar-aluno.svg";
    public static final String AVATAR_PROFESSOR = "avatar-professor.svg";

    // Caminho relativo à raiz do frontend (frontend/public), exibido direto no <img [src]>.
    public static String imagemPadrao(FuncaoDeUsuario funcao) {
        return funcao == FuncaoDeUsuario.PROFESSOR ? AVATAR_PROFESSOR : AVATAR_ALUNO;
    }

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
            throw new ConflictException("Já existe um usuário cadastrado com esse e-mail.");
        }
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        if (usuario.getCriado() == null) {
            usuario.setCriado(new Date());
        }
        if (usuario.getImagemPerfil() == null || usuario.getImagemPerfil().isBlank()) {
            usuario.setImagemPerfil(imagemPadrao(usuario.getFuncao()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(String id, Usuario usuario) {
        return usuarioRepository.findById(id).map(existing -> {
            if (usuario.getEmail() != null) {
                usuarioRepository.findByEmail(usuario.getEmail())
                        .filter(outro -> !outro.getId().equals(id))
                        .ifPresent(outro -> {
                            throw new ConflictException("Já existe outro usuário cadastrado com esse e-mail.");
                        });
            }
            // Atualiza apenas campos fornecidos; mantém a senha se não vier na requisição.
            // A função (aluno/professor) é definida no cadastro e não muda pela edição de perfil.
            if (usuario.getNome() != null) {
                existing.setNome(usuario.getNome());
            }
            if (usuario.getEmail() != null) {
                existing.setEmail(usuario.getEmail());
            }
            if (usuario.getMatricula() != null) {
                existing.setMatricula(usuario.getMatricula());
            }
            if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
                existing.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }
            if (usuario.getImagemPerfil() != null) {
                existing.setImagemPerfil(usuario.getImagemPerfil().isBlank()
                        ? imagemPadrao(existing.getFuncao())
                        : usuario.getImagemPerfil());
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

    public Usuario removerImagemPerfil(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        usuario.setImagemPerfil(imagemPadrao(usuario.getFuncao()));
        return usuarioRepository.save(usuario);
    }

    public int preencherImagensPadrao() {
        List<Usuario> semImagem = usuarioRepository.findByImagemPerfilIsNullOrImagemPerfil("");
        semImagem.forEach(u -> u.setImagemPerfil(imagemPadrao(u.getFuncao())));
        usuarioRepository.saveAll(semImagem);
        return semImagem.size();
    }

    // Senhas gravadas antes do BCrypt ficaram em texto puro. Converte todas de uma vez para o
    // login não precisar mais aceitar texto puro (M5).
    public int migrarSenhasLegado() {
        List<Usuario> legado = usuarioRepository.findAll().stream()
                .filter(u -> u.getSenha() != null && !u.getSenha().isBlank())
                .filter(u -> !ehHashBcrypt(u.getSenha()))
                .toList();
        legado.forEach(u -> u.setSenha(passwordEncoder.encode(u.getSenha())));
        usuarioRepository.saveAll(legado);
        return legado.size();
    }

    static boolean ehHashBcrypt(String senha) {
        return senha != null && senha.matches("^\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }

    public boolean deleteUsuario(String id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
