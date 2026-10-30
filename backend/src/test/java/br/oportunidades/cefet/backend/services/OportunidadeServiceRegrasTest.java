package br.oportunidades.cefet.backend.services;

import br.oportunidades.cefet.backend.enums.GrandeAreaConhecimento;
import br.oportunidades.cefet.backend.enums.StatusCandidatura;
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
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Regras de negócio validadas na revisão dos fluxos de oportunidade e candidatura (M1).
@ExtendWith(MockitoExtension.class)
class OportunidadeServiceRegrasTest {

    private static final long DIA = 24L * 60 * 60 * 1000;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private FeedService feedService;
    @Mock private CandidaturaRepository candidaturaRepository;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks
    private OportunidadeService service;

    private Oportunidade novaValida(int vagas) {
        Date agora = new Date();
        return Oportunidade.builder()
                .nome("Monitoria de Cálculo")
                .professorId("prof-1")
                .idCategoria("MONITORIA")
                .grandesAreas(List.of(GrandeAreaConhecimento.values()[0]))
                .quantidadeDeVagas(vagas)
                .dataInicioInscricao(new Date(agora.getTime() - DIA))
                .dataFimInscricao(new Date(agora.getTime() + DIA))
                .build();
    }

    @Test
    void criarSemVagasEhRecusado() {
        IllegalArgumentException erro =
                assertThrows(IllegalArgumentException.class, () -> service.salvar(novaValida(0)));

        assertEquals("A oportunidade precisa ter ao menos 1 vaga.", erro.getMessage());
        verifyNoInteractions(oportunidadeRepository);
    }

    @Test
    void criarIgnoraVagasPreenchidasEFinalizadaDoCliente() {
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));
        Oportunidade enviada = novaValida(3);
        enviada.setVagasPreenchidas(3);
        enviada.setFinalizada(true);

        Oportunidade salva = service.salvar(enviada);

        assertEquals(0, salva.getVagasPreenchidas());
        assertFalse(salva.getFinalizada());
        verify(feedService).criarFeedOportunidade(salva);
    }

    @Test
    void editarNaoTrocaDonoNemVagasPreenchidas() {
        Oportunidade existente = novaValida(3);
        existente.setId("op-1");
        existente.setVagasPreenchidas(1);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(existente));
        when(oportunidadeRepository.save(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));

        Oportunidade enviada = novaValida(5);
        enviada.setProfessorId("intruso");
        enviada.setVagasPreenchidas(0);
        enviada.setNome("Novo nome");

        Oportunidade salva = service.atualizar("op-1", enviada).orElseThrow();

        assertEquals("prof-1", salva.getProfessorId());
        assertEquals(1, salva.getVagasPreenchidas());
        assertEquals(5, salva.getQuantidadeDeVagas());
        assertEquals("Novo nome", salva.getNome());
    }

    @Test
    void editarNaoDeixaVagasAbaixoDasPreenchidas() {
        Oportunidade existente = novaValida(3);
        existente.setId("op-1");
        existente.setVagasPreenchidas(2);
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.atualizar("op-1", novaValida(1)));
        verify(oportunidadeRepository, never()).save(any());
    }

    @Test
    void candidaturaForaDoPeriodoEhRecusada() {
        Oportunidade encerrada = novaValida(2);
        encerrada.setId("op-1");
        encerrada.setDataFimInscricao(new Date(System.currentTimeMillis() - DIA / 2));
        encerrada.setDataInicioInscricao(new Date(System.currentTimeMillis() - 2 * DIA));
        when(oportunidadeRepository.findById("op-1")).thenReturn(Optional.of(encerrada));

        assertThrows(IllegalStateException.class, () -> service.candidatar("op-1", "aluno-1"));
        verify(candidaturaRepository, never()).save(any());
    }

    @Test
    void candidaturasDoAlunoSaoPaginadas() {
        List<Candidatura> candidaturas = List.of(
                Candidatura.builder().alunoId("aluno-1").oportunidadeId("a").status(StatusCandidatura.CONCORRENDO).build(),
                Candidatura.builder().alunoId("aluno-1").oportunidadeId("b").status(StatusCandidatura.APROVADO).build(),
                Candidatura.builder().alunoId("aluno-1").oportunidadeId("c").status(StatusCandidatura.RESERVA).build());
        when(candidaturaRepository.findByAlunoId("aluno-1")).thenReturn(candidaturas);
        when(oportunidadeRepository.findAllById(List.of("a", "b", "c"))).thenReturn(List.of(
                Oportunidade.builder().id("a").criado(new Date(1000)).build(),
                Oportunidade.builder().id("b").criado(new Date(3000)).build(),
                Oportunidade.builder().id("c").criado(new Date(2000)).build()));

        Page<Oportunidade> pagina0 = service.listarPorAluno("aluno-1", 0, 2);
        Page<Oportunidade> pagina1 = service.listarPorAluno("aluno-1", 1, 2);

        assertEquals(3, pagina0.getTotalElements());
        assertEquals(2, pagina0.getTotalPages());
        assertEquals(List.of("b", "c"), pagina0.getContent().stream().map(Oportunidade::getId).toList());
        assertEquals(List.of("a"), pagina1.getContent().stream().map(Oportunidade::getId).toList());
        assertEquals(StatusCandidatura.APROVADO, pagina0.getContent().get(0).getStatusCandidaturaAluno());
    }
}
