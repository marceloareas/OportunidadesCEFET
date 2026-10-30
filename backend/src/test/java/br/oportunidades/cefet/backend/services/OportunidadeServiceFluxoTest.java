package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import br.oportunidades.cefet.backend.enums.StatusOportunidade;
import br.oportunidades.cefet.backend.models.Candidatura;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.repositories.CandidaturaRepository;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Fluxo completo de candidatura: inscrição → aprovação → finalização (M2).
@ExtendWith(MockitoExtension.class)
class OportunidadeServiceFluxoTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private FeedService feedService;
    @Mock private CandidaturaRepository candidaturaRepository;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks
    private OportunidadeService service;

    private Oportunidade oportunidade(int vagas, int preenchidas, boolean finalizada) {
        return Oportunidade.builder()
                .id("op-1").nome("Pesquisa em IA").professorId("prof-1")
                .quantidadeDeVagas(vagas).vagasPreenchidas(preenchidas).finalizada(finalizada)
                .build();
    }

    private Candidatura candidatura(String alunoId, StatusCandidatura status) {
        return Candidatura.builder().alunoId(alunoId).oportunidadeId("op-1").status(status).build();
    }

    private void salvarDevolveOArgumento() {
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void candidaturaNovaNasceConcorrendo() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, false)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1")).thenReturn(Optional.empty());

        service.candidatar("op-1", "aluno-1");

        verify(candidaturaRepository).save(argThat(c ->
                c.getAlunoId().equals("aluno-1") && c.getStatus() == StatusCandidatura.CONCORRENDO));
    }

    @Test
    void naoCandidataEmOportunidadeFinalizada() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, true)));

        assertThrows(IllegalStateException.class, () -> service.candidatar("op-1", "aluno-1"));
        verify(candidaturaRepository, never()).save(any());
    }

    @Test
    void candidatarEmOportunidadeInexistenteDevolveVazio() {
        when(oportunidadeRepository.findById("op-x")).thenReturn(Optional.empty());

        assertTrue(service.candidatar("op-x", "aluno-1").isEmpty());
    }

    @Test
    void aprovarMarcaAprovadoESomaVaga() {
        Candidatura c = candidatura("aluno-1", StatusCandidatura.CONCORRENDO);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, false)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1")).thenReturn(Optional.of(c));
        salvarDevolveOArgumento();

        Oportunidade salva = service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1").orElseThrow();

        assertEquals(StatusCandidatura.APROVADO, c.getStatus());
        assertEquals(1, salva.getVagasPreenchidas());
        assertFalse(salva.getFinalizada());
        assertEquals(StatusOportunidade.INSCRICOES_ABERTAS, salva.getStatus());
    }

    @Test
    void ultimaVagaFinalizaEMandaRestanteParaReserva() {
        Candidatura aprovada = candidatura("aluno-1", StatusCandidatura.CONCORRENDO);
        Candidatura sobrando = candidatura("aluno-2", StatusCandidatura.CONCORRENDO);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(1, 0, false)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1")).thenReturn(Optional.of(aprovada));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(aprovada, sobrando));
        salvarDevolveOArgumento();

        Oportunidade salva = service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1").orElseThrow();

        assertTrue(salva.getFinalizada());
        assertEquals(StatusOportunidade.FINALIZADA, salva.getStatus());
        assertEquals(StatusCandidatura.APROVADO, aprovada.getStatus());
        assertEquals(StatusCandidatura.RESERVA, sobrando.getStatus());
    }

    @Test
    void naoAprovaQuemNaoSeCandidatou() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, false)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-9", "op-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.aprovarCandidatoDoProfessor("op-1", "aluno-9", "prof-1"));
    }

    @Test
    void naoAprovaDuasVezes() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 1, false)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1"))
                .thenReturn(Optional.of(candidatura("aluno-1", StatusCandidatura.APROVADO)));

        assertThrows(IllegalStateException.class,
                () -> service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1"));
        verify(oportunidadeRepository, never()).save(any());
    }

    @Test
    void outroProfessorNaoAprovaNoServico() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, false)));

        assertThrows(SecurityException.class,
                () -> service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-2"));
    }

    @Test
    void naoAprovaEmOportunidadeFinalizada() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, true)));

        assertThrows(IllegalStateException.class,
                () -> service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1"));
    }

    @Test
    void finalizarManualMandaConcorrentesParaReserva() {
        Candidatura concorrendo = candidatura("aluno-1", StatusCandidatura.CONCORRENDO);
        Candidatura aprovado = candidatura("aluno-2", StatusCandidatura.APROVADO);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(3, 1, false)));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(concorrendo, aprovado));
        salvarDevolveOArgumento();

        Oportunidade salva = service.finalizar("op-1").orElseThrow();

        assertTrue(salva.getFinalizada());
        assertEquals(StatusCandidatura.RESERVA, concorrendo.getStatus());
        assertEquals(StatusCandidatura.APROVADO, aprovado.getStatus());
        verify(feedService).atualizarFeedOportunidade(salva);
    }

    // Sprint 5 (teste do zero): a resposta de finalizar/aprovar vinha sem o nome do criador e
    // a tela, que troca o cartão pela resposta, mostrava "é você" sem o nome.
    @Test
    void respostaDeFinalizarEAprovarTrazNomeDoCriadorECandidatos() {
        Candidatura concorrendo = candidatura("aluno-1", StatusCandidatura.CONCORRENDO);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0, false)));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(concorrendo));
        when(usuarioRepository.findById("prof-1")).thenReturn(Optional.of(
                br.oportunidades.cefet.backend.models.Usuario.builder()
                        .id("prof-1").nome("Prof Teste").imagemPerfil("prof.png").build()));
        salvarDevolveOArgumento();

        Oportunidade finalizada = service.finalizar("op-1").orElseThrow();

        assertEquals("Prof Teste", finalizada.getNomeCriador());
        assertEquals("prof.png", finalizada.getImagemPerfil());
        assertEquals(List.of("aluno-1"), finalizada.getAlunosCandidatosId());
    }

    @Test
    void naoFinalizaDuasVezes() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(3, 1, true)));

        assertThrows(IllegalStateException.class, () -> service.finalizar("op-1"));
    }

    @Test
    void naoFinalizaComTodasAsVagasPreenchidas() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 2, false)));

        assertThrows(IllegalStateException.class, () -> service.finalizar("op-1"));
    }

    @Test
    void likeAlternaEntreAdicionarERemover() {
        Oportunidade op = oportunidade(2, 0, false);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(op));
        salvarDevolveOArgumento();

        assertEquals("Like adicionado.", service.alternarLike("op-1", "aluno-1"));
        assertTrue(op.getIdLikes().contains("aluno-1"));
        assertEquals("Like removido.", service.alternarLike("op-1", "aluno-1"));
        assertFalse(op.getIdLikes().contains("aluno-1"));
    }
}
