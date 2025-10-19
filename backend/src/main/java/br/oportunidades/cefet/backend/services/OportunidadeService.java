package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;

@Service
public class OportunidadeService {

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    public OportunidadeService(OportunidadeRepository oportunidadeRepository) {
        this.oportunidadeRepository = oportunidadeRepository;
    }

    public List<Oportunidade> listarTodas() {
        List<Oportunidade> oportunidades = oportunidadeRepository.findAll();
        // Ordena manualmente pelo campo "criado"
        oportunidades.sort((a, b) -> b.getCriado().compareTo(a.getCriado()));
        return oportunidades;
    }

    public Optional<Oportunidade> buscarPorId(String id) {
        return oportunidadeRepository.findById(id);
    }

    public Oportunidade criar(Oportunidade oportunidade) {
        oportunidade.setCriado(new Date());
        oportunidade.setVagasPreenchidas(0); // sempre começa vazia
        return oportunidadeRepository.save(oportunidade);
    }

    public Oportunidade candidatar(String idOportunidade, String idAluno) {
        Oportunidade oportunidade = oportunidadeRepository.findById(idOportunidade)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (oportunidade.getVagasPreenchidas() >= oportunidade.getQuantidadeDeVagas()) {
            throw new RuntimeException("Todas as vagas estão preenchidas");
        }

        // Adiciona o aluno à lista de candidatos (se ainda não estiver)
        if (!oportunidade.getIdCandidatos().contains(idAluno)) {
            oportunidade.getIdCandidatos().add(idAluno);
            oportunidade.setVagasPreenchidas(oportunidade.getIdCandidatos().size());
        }

        return oportunidadeRepository.save(oportunidade);
    }

    public void deletar(String id) {
        oportunidadeRepository.deleteById(id);
    }
}
