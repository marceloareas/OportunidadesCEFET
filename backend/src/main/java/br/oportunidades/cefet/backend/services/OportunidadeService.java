package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.CategoriaOportunidade;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.CandidaturaRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import br.oportunidades.cefet.backend.models.Candidatura;
import br.oportunidades.cefet.backend.enums.StatusCandidatura;

@Service
public class OportunidadeService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OportunidadeRepository oportunidadeRepository;

    @Autowired
    private FeedService feedService;

    @Autowired
    private CandidaturaRepository candidaturaRepository;

    public Page<Oportunidade> listarTodos(int page, int size) {
            Page<Oportunidade> ops = oportunidadeRepository.findAllByOrderByCriadoDesc(PageRequest.of(page, size));
            ops.getContent().forEach(this::preencherDadosCriador);
            return ops;
    }

    public Optional<Oportunidade> buscarPorId(String id) {
            Optional<Oportunidade> opt = oportunidadeRepository.findById(id);
            opt.ifPresent(this::preencherDadosCriador);
            return opt;
        }

    public Oportunidade salvar(Oportunidade oportunidade) {
        if (oportunidade.getIdCategoria() == null || oportunidade.getIdCategoria().isBlank()) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }

        if (oportunidade.getGrandesAreas() == null || oportunidade.getGrandesAreas().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma grande área do conhecimento.");
        }

        validarPeriodoInscricao(oportunidade);

        try {
            CategoriaOportunidade.valueOf(oportunidade.getIdCategoria().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Categoria inválida. Use: MONITORIA, EXTENSAO, PESQUISA, ESTAGIO ou ORIENTACAO_TCC"
            );
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

        if (oportunidade.getFinalizada() == null) {
            oportunidade.setFinalizada(false);
        }

        Oportunidade salva = oportunidadeRepository.save(oportunidade);

        aplicarStatus(salva);

        feedService.criarFeedOportunidade(salva);

        return salva;
    }

    public Page<Oportunidade> listarPorProfessor(String idProfessor, int page, int size) {
        Page<Oportunidade> ops = oportunidadeRepository
                .findByProfessorIdOrderByCriadoDesc(idProfessor, PageRequest.of(page, size));
        ops.getContent().forEach(this::preencherDadosCriador);
        return ops;
    }

    public Page<Oportunidade> listarPorAluno(String idAluno, int page, int size) {

        List<Candidatura> candidaturas =
                candidaturaRepository.findByAlunoId(idAluno);

        List<String> oportunidadesIds = candidaturas.stream()
                .map(Candidatura::getOportunidadeId)
                .toList();

        List<Oportunidade> oportunidades =
                oportunidadeRepository.findAllById(oportunidadesIds);

        oportunidades.forEach(this::preencherDadosCriador);

        return new PageImpl<>(
                oportunidades,
                PageRequest.of(page, size),
                oportunidades.size()
        );
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

            if (atualizada.getDataInicioInscricao() != null) {
                existente.setDataInicioInscricao(atualizada.getDataInicioInscricao());
            }

            if (atualizada.getDataFimInscricao() != null) {
                existente.setDataFimInscricao(atualizada.getDataFimInscricao());
            }

            validarPeriodoOpcional(existente);

            if (existente.getIdLikes() == null) {
                existente.setIdLikes(new ArrayList<>());
            }

            Oportunidade salva = oportunidadeRepository.save(existente);

            aplicarStatus(salva);

            feedService.atualizarFeedOportunidade(salva);

            return salva;
        });
    }

    public void deletar(String id) {
        oportunidadeRepository.deleteById(id);
        feedService.deletarFeedItem(id);
    }

    public Optional<Oportunidade> candidatar(String idOportunidade, String idAluno) {

        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);

        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Oportunidade oportunidade = opt.get();

        if (Boolean.TRUE.equals(oportunidade.getFinalizada())) {
            throw new IllegalStateException(
                    "Oportunidade finalizada. Não é possível se candidatar."
            );
        }

        if (!OportunidadeStatusHelper.estaComInscricoesAbertas(oportunidade)) {
            throw new IllegalStateException(
                    "As inscrições não estão abertas para esta oportunidade."
            );
        }

        Optional<Candidatura> candidaturaExistente =
                candidaturaRepository.findByAlunoIdAndOportunidadeId(
                        idAluno,
                        idOportunidade
                );

        if (candidaturaExistente.isPresent()) {
            return Optional.of(oportunidade);
        }

        Candidatura candidatura = Candidatura.builder()
                .alunoId(idAluno)
                .oportunidadeId(idOportunidade)
                .status(StatusCandidatura.CONCORRENDO)
                .build();

        candidaturaRepository.save(candidatura);

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
            Oportunidade salva = oportunidadeRepository.save(oportunidade);

            feedService.atualizarFeedOportunidade(salva);

            return "Like removido.";
        } else {
            likes.add(idUsuario);
            oportunidade.setIdLikes(likes);
            Oportunidade salva = oportunidadeRepository.save(oportunidade);

            feedService.atualizarFeedOportunidade(salva);

            return "Like adicionado.";
        }
    }

    public Page<Usuario> listarCandidatos(String idOportunidade, int page, int size) {

        List<Candidatura> candidaturas =
                candidaturaRepository.findByOportunidadeId(idOportunidade);

        if (candidaturas.isEmpty()) {
            return Page.empty();
        }

        List<String> ids = candidaturas.stream()
                .map(Candidatura::getAlunoId)
                .toList();

        PageRequest pageable = PageRequest.of(page, size);

        return usuarioRepository.findByIdIn(ids, pageable);
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

        List<Candidatura> candidaturas =
        candidaturaRepository.findByOportunidadeId(idOportunidade);

        if (candidaturas.isEmpty()) {
            return Optional.of(Collections.emptyList());
        }

        List<String> ids = candidaturas.stream()
                .map(Candidatura::getAlunoId)
                .toList();

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

        Optional<Candidatura> candidaturaOpt =
                candidaturaRepository.findByAlunoIdAndOportunidadeId(
                        idAluno,
                        idOportunidade
                );

        if (candidaturaOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "Aluno não está concorrendo nessa oportunidade."
            );
        }

        Candidatura candidatura = candidaturaOpt.get();

        if (candidatura.getStatus() == StatusCandidatura.APROVADO) {
            throw new IllegalStateException("Aluno já foi aprovado.");
        }

        candidatura.setStatus(StatusCandidatura.APROVADO);
        candidaturaRepository.save(candidatura);
        oportunidade.setVagasPreenchidas(
                oportunidade.getVagasPreenchidas() + 1
        );

        if (oportunidade.getVagasPreenchidas() >= oportunidade.getQuantidadeDeVagas()) {

            oportunidade.setFinalizada(true);

            List<Candidatura> candidaturas =
                    candidaturaRepository.findByOportunidadeId(idOportunidade);

            for (Candidatura c : candidaturas) {

                if (c.getStatus() == StatusCandidatura.CONCORRENDO) {

                    c.setStatus(StatusCandidatura.RESERVA);

                    candidaturaRepository.save(c);
                }
            }
        }

        Oportunidade salva = oportunidadeRepository.save(oportunidade);

        aplicarStatus(salva);

        feedService.atualizarFeedOportunidade(salva);

        return Optional.of(salva);
    }

    public Optional<Oportunidade> finalizar(String idOportunidade) {
        Optional<Oportunidade> opt = oportunidadeRepository.findById(idOportunidade);
        if (opt.isEmpty()) return Optional.empty();

        Oportunidade oportunidade = opt.get();

        if (Boolean.TRUE.equals(oportunidade.getFinalizada())) {
            throw new IllegalStateException("Oportunidade já está finalizada.");
        }

        if (oportunidade.getVagasPreenchidas() >= oportunidade.getQuantidadeDeVagas()) {
            throw new IllegalStateException("Todas as vagas já foram preenchidas.");
        }

        List<Candidatura> candidaturas =
        candidaturaRepository.findByOportunidadeId(idOportunidade);

        for (Candidatura candidatura : candidaturas) {

            if (candidatura.getStatus() == StatusCandidatura.CONCORRENDO) {

                candidatura.setStatus(StatusCandidatura.RESERVA);

                candidaturaRepository.save(candidatura);
            }
        }

        oportunidade.setFinalizada(true);
        Oportunidade salva = oportunidadeRepository.save(oportunidade);

        aplicarStatus(salva);

        feedService.atualizarFeedOportunidade(salva);

        return Optional.of(salva);
    }

    private void preencherDadosCriador(Oportunidade op) {
        if (op.getProfessorId() != null) {
            usuarioRepository.findById(op.getProfessorId()).ifPresent(criador -> {
                op.setNomeCriador(criador.getNome());
                op.setImagemPerfil(criador.getImagemPerfil());
            });
        }

        aplicarStatus(op);
    }

    private void aplicarStatus(Oportunidade oportunidade) {
        OportunidadeStatusHelper.aplicarStatus(oportunidade);
    }

    private void validarPeriodoInscricao(Oportunidade oportunidade) {
        if (oportunidade.getDataInicioInscricao() == null || oportunidade.getDataFimInscricao() == null) {
            throw new IllegalArgumentException("Informe a data inicial e final das inscrições.");
        }

        validarPeriodoOpcional(oportunidade);
    }

    private void validarPeriodoOpcional(Oportunidade oportunidade) {
        if (oportunidade.getDataInicioInscricao() == null || oportunidade.getDataFimInscricao() == null) {
            return;
        }

        if (oportunidade.getDataFimInscricao().before(oportunidade.getDataInicioInscricao())) {
            throw new IllegalArgumentException("A data final das inscrições deve ser igual ou posterior à data inicial.");
        }
    }
}
