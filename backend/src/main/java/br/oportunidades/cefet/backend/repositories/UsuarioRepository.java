package br.oportunidades.cefet.backend.repositories;

import br.oportunidades.cefet.backend.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

	java.util.Optional<Usuario> findByEmail(String email);

	Page<Usuario> findByIdIn(List<String> ids, Pageable pageable);
}
