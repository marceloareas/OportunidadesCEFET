package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Categoria;
import br.oportunidades.cefet.backend.repositories.CategoriaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    @Autowired
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() { return categoriaRepository.findAll(); }
    public Optional<Categoria> buscarPorId(String id) { return categoriaRepository.findById(id); }
    public Categoria salvar(Categoria categoria) { return categoriaRepository.save(categoria); }
    public void deletar(String id) { categoriaRepository.deleteById(id); }

    
}
