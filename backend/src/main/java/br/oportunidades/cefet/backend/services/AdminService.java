package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

// Sprint 6: moderação de usuários pelo administrador. Quem chama já passou por
// PermissoesUsuario.exigirAdmin no controller.
@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;

    public AdminService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Page<Usuario> listarUsuarios(String busca, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));
        String termo = busca == null ? "" : busca.trim();
        if (termo.isEmpty()) {
            return usuarioRepository.findAll(pageable);
        }
        return usuarioRepository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(termo, termo, pageable);
    }

    public Usuario desativar(Usuario admin, String idUsuario) {
        Usuario alvo = buscar(idUsuario);
        if (alvo.getId().equals(admin.getId())) {
            throw new SecurityException("Você não pode desativar a sua própria conta.");
        }
        if (alvo.getFuncao() == FuncaoDeUsuario.ADMIN) {
            throw new SecurityException("Não é possível desativar outro administrador.");
        }
        alvo.setAtivo(false);
        return usuarioRepository.save(alvo);
    }

    public Usuario reativar(String idUsuario) {
        Usuario alvo = buscar(idUsuario);
        alvo.setAtivo(true);
        return usuarioRepository.save(alvo);
    }

    private Usuario buscar(String idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}
