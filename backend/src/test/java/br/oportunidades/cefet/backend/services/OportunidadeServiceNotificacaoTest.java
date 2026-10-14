package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.StatusCandidatura;
import br.oportunidades.cefet.backend.models.Candidatura;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.TipoNotificacao;
import br.oportunidades.cefet.backend.models.Usuario;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OportunidadeServiceNotificacaoTest {

    private static final String MSG_RESERVA =
            "A oportunidade \"Monitoria de Cálculo\" foi finalizada. Você ficou na lista de reserva.";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private FeedService feedService;
    @Mock private CandidaturaRepository candidaturaRepository;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks
    private OportunidadeService service;

    private Oportunidade oportunidade(int vagas, int preenchidas) {
        return Oportunidade.builder()
                .id("op-1")
                .nome("Monitoria de Cálculo")
                .professorId("prof-1")
                .quantidadeDeVagas(vagas)
                .vagasPreenchidas(preenchidas)
                .finalizada(false)
                .build();
    }

    private Candidatura candidatura(String alunoId, StatusCandidatura status) {
        return Candidatura.builder().alunoId(alunoId).oportunidadeId("op-1").status(status).build();
    }

    @Test
    void candidaturaNovaNotificaOProfessor() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1")).thenReturn(Optional.empty());
        when(usuarioRepository.findById("aluno-1"))
                .thenReturn(Optional.of(Usuario.builder().id("aluno-1").nome("Bia").build()));

        service.candidatar("op-1", "aluno-1");

        verify(notificacaoService).notificar(
                "prof-1",
                TipoNotificacao.CANDIDATURA_RECEBIDA,
                "Bia se candidatou à oportunidade \"Monitoria de Cálculo\".",
                "op-1");
    }

    @Test
    void candidaturaRepetidaNaoNotifica() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1"))
                .thenReturn(Optional.of(candidatura("aluno-1", StatusCandidatura.CONCORRENDO)));

        service.candidatar("op-1", "aluno-1");

        verifyNoInteractions(notificacaoService);
    }

    @Test
    void aprovarNotificaOAlunoESemFinalizarNaoNotificaReservas() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(2, 0)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1"))
                .thenReturn(Optional.of(candidatura("aluno-1", StatusCandidatura.CONCORRENDO)));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));

        service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1");

        verify(notificacaoService).notificar(
                "aluno-1",
                TipoNotificacao.CANDIDATURA_APROVADA,
                "Você foi aprovado na oportunidade \"Monitoria de Cálculo\".",
                "op-1");
        verify(notificacaoService, never())
                .notificar(anyString(), eq(TipoNotificacao.OPORTUNIDADE_FINALIZADA), anyString(), anyString());
    }

    @Test
    void aprovacaoQuePreencheAsVagasNotificaOsReservas() {
        Candidatura aprovado = candidatura("aluno-1", StatusCandidatura.CONCORRENDO);
        Candidatura outro = candidatura("aluno-2", StatusCandidatura.CONCORRENDO);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(1, 0)));
        when(candidaturaRepository.findByAlunoIdAndOportunidadeId("aluno-1", "op-1")).thenReturn(Optional.of(aprovado));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(aprovado, outro));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));

        service.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1");

        verify(notificacaoService).notificar("aluno-2", TipoNotificacao.OPORTUNIDADE_FINALIZADA, MSG_RESERVA, "op-1");
        verify(notificacaoService, never())
                .notificar(eq("aluno-1"), eq(TipoNotificacao.OPORTUNIDADE_FINALIZADA), anyString(), anyString());
    }

    @Test
    void finalizarNotificaSomenteQuemFicouNaReserva() {
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(oportunidade(3, 1)));
        when(candidaturaRepository.findByOportunidadeId("op-1")).thenReturn(List.of(
                candidatura("aluno-1", StatusCandidatura.APROVADO),
                candidatura("aluno-2", StatusCandidatura.CONCORRENDO)));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));

        service.finalizar("op-1");

        verify(notificacaoService).notificar("aluno-2", TipoNotificacao.OPORTUNIDADE_FINALIZADA, MSG_RESERVA, "op-1");
        verify(notificacaoService, times(1)).notificar(anyString(), any(), anyString(), anyString());
    }
}
