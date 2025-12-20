package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.CategoriaOportunidade;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OportunidadeService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    public List<Oportunidade> listarTodos() {
        return oportunidadeRepository.findAll();
    }

    public Optional<Oportunidade> buscarPorId(String id) {
        return oportunidadeRepository.findById(id);
    }

    public Oportunidade salvar(Oportunidade oportunidade) {
        if (oportunidade.getIdCategoria() == null || oportunidade.getIdCategoria().isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }

        if (oportunidade.getGrandesAreas() == null || oportunidade.getGrandesAreas().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma grande área do conhecimento.");
        }

        try {
            CategoriaOportunidade.valueOf(oportunidade.getIdCategoria().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Categoria inválida. Use: MONITORIA, EXTENSAO, PESQUISA, ESTAGIO ou ORIENTACAO_TCC"
            );
        }

        if (oportunidade.getAlunosCandidatosId() == null) {
            oportunidade.setAlunosCandidatosId(new ArrayList<>());
        }

        if (oportunidade.getIdLikes() == null) {
            oportunidade.setIdLikes(new ArrayList<>());
        }

        if (oportunidade.getCriado() == null) {
            oportunidade.setCriado(new Date());
        }

        if (oportunidade.getVagasPreenchidas() == null) {
            oportunidade.setVagasPreenchidas(0);
        }

        if (oportunidade.getQuantidadeDeVagas() == null) {
            oportunidade.setQuantidadeDeVagas(0);
        }

        return oportunidadeRepository.save(oportunidade);
    }

    public Optional<Oportunidade> atualizar(String id, Oportunidade atualizada) {
        return oportunidadeRepository.findById(id).map(existente -> {
            existente.setNome(atualizada.getNome());
            existente.setDescricao(atualizada.getDescricao());
            existente.setProfessorId(atualizada.getProfessorId());
            existente.setQuantidadeDeVagas(atualizada.getQuantidadeDeVagas());
            existente.setVagasPreenchidas(atualizada.getVagasPreenchidas());
            existente.setIdCategoria(atualizada.getIdCategoria());
            existente.setImagemBase64(atualizada.getImagemBase64());

            if (existente.getAlunosCandidatosId() == null) {
                existente.setAlunosCandidatosId(new ArrayList<>());
            }

            if (existente.getIdLikes() == null) {
                existente.setIdLikes(new ArrayList<>());
            }

            return oportunidadeRepository.save(existente);
        });
    }

    public void deletar(String id) {
        oportunidadeRepository.deleteById(id);
    }

    public Optional<Oportunidade> candidatar(String idOportunidade, String idAluno) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) return Optional.empty();

        Oportunidade oportunidade = opt.get();
        List<String> candidatos = oportunidade.getAlunosCandidatosId();

        if (Boolean.TRUE.equals(oportunidade.getFinalizada())) {
            throw new IllegalStateException("Oportunidade finalizada. Não é possível se candidatar.");
        }

        if (candidatos == null) candidatos = new ArrayList<>();

        if (!candidatos.contains(idAluno)) {
            candidatos.add(idAluno);
            oportunidade.setAlunosCandidatosId(candidatos);
            oportunidadeRepository.save(oportunidade);
        }

        return Optional.of(oportunidade);
    }

    public String alternarLike(String idOportunidade, String idUsuario) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) {
            return "Oportunidade não encontrada.";
        }

        Oportunidade oportunidade = opt.get();
        List<String> likes = oportunidade.getIdLikes();

        if (likes == null) likes = new ArrayList<>();

        if (likes.contains(idUsuario)) {
            likes.remove(idUsuario);
            oportunidade.setIdLikes(likes);
            oportunidadeRepository.save(oportunidade);
            return "Like removido.";
        } else {
            likes.add(idUsuario);
            oportunidade.setIdLikes(likes);
            oportunidadeRepository.save(oportunidade);
            return "Like adicionado.";
        }
    }

    public Optional<List<Usuario>> listarCandidatos(String idOportunidade) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) return Optional.empty();

        Oportunidade oportunidade = opt.get();
        List<String> ids = oportunidade.getAlunosCandidatosId();

        if (ids == null || ids.isEmpty()) {
            return Optional.of(Collections.emptyList());
        }

        List<Usuario> usuarios = usuarioRepository.findAllById(ids);
        return Optional.of(usuarios);
    }

    /**
     * Lista candidatos apenas se o solicitante for o professor dono da oportunidade.
     */
    public Optional<List<Usuario>> listarCandidatosDoProfessor(String idOportunidade, String idProfessor) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) return Optional.empty();

        Oportunidade oportunidade = opt.get();

        if (oportunidade.getProfessorId() == null || !oportunidade.getProfessorId().equals(idProfessor)) {
            throw new SecurityException("Apenas o criador da oportunidade pode ver os candidatos.");
        }

        List<String> ids = oportunidade.getAlunosCandidatosId();
        if (ids == null || ids.isEmpty()) {
            return Optional.of(Collections.emptyList());
        }
        List<Usuario> usuarios = usuarioRepository.findAllById(ids);
        return Optional.of(usuarios);
    }

    public Optional<Oportunidade> aprovarCandidato(String idOportunidade, String idAluno) {
        return aprovarCandidatoDoProfessor(idOportunidade, idAluno, null);
    }

    public Optional<Oportunidade> aprovarCandidatoDoProfessor(String idOportunidade, String idAluno, String idProfessor) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) return Optional.empty();

        Oportunidade oportunidade = opt.get();

        if (idProfessor != null && oportunidade.getProfessorId() != null
                && !oportunidade.getProfessorId().equals(idProfessor)) {
            throw new SecurityException("Apenas o criador da oportunidade pode aprovar candidatos.");
        }

        if (Boolean.TRUE.equals(oportunidade.getFinalizada())) {
            throw new IllegalStateException("Oportunidade finalizada.");
        }

        if (oportunidade.getVagasPreenchidas() >= oportunidade.getQuantidadeDeVagas()) {
            throw new IllegalStateException("Todas as vagas já foram preenchidas.");
        }

        if (!oportunidade.getAlunosCandidatosId().contains(idAluno)) {
            throw new IllegalArgumentException("Aluno não está na lista de candidatos.");
        }

        if (oportunidade.getAlunosAprovadosId().contains(idAluno)) {
            throw new IllegalStateException("Aluno já foi aprovado.");
        }

        oportunidade.getAlunosAprovadosId().add(idAluno);
        oportunidade.setVagasPreenchidas(
                oportunidade.getVagasPreenchidas() + 1
        );

        if (oportunidade.getVagasPreenchidas() >= oportunidade.getQuantidadeDeVagas()) {
            oportunidade.setFinalizada(true);
        }

        oportunidadeRepository.save(oportunidade);
        return Optional.of(oportunidade);
    }

    public Optional<Oportunidade> finalizar(String idOportunidade) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) return Optional.empty();

        Oportunidade oportunidade = opt.get();

        if (Boolean.TRUE.equals(oportunidade.getFinalizada())) {
            throw new IllegalStateException("Oportunidade já está finalizada.");
        }

        oportunidade.setFinalizada(true);
        oportunidadeRepository.save(oportunidade);

        return Optional.of(oportunidade);
    }
}
