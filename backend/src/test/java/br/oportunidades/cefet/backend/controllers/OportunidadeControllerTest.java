package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.OportunidadePermissoes;
import br.oportunidades.cefet.backend.services.OportunidadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Ponta a ponta do controller com as permissões reais (M2 sobre as regras do M1).
@ExtendWith(MockitoExtension.class)
class OportunidadeControllerTest {

    @Mock private OportunidadeService oportunidadeService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;

    private MockMvc mvc;

    private final Usuario professor = Usuario.builder()
            .id("prof-1").email("prof@cefet.br").funcao(FuncaoDeUsuario.PROFESSOR).build();
    private final Usuario outroProfessor = Usuario.builder()
            .id("prof-2").email("outro@cefet.br").funcao(FuncaoDeUsuario.PROFESSOR).build();
    private final Usuario aluno = Usuario.builder()
            .id("aluno-1").email("aluno@cefet.br").funcao(FuncaoDeUsuario.ALUNO).build();

    @BeforeEach
    void setup() {
        OportunidadePermissoes permissoes = new OportunidadePermissoes(usuarioRepository, oportunidadeRepository);
        mvc = MockMvcBuilders
                .standaloneSetup(new OportunidadeController(oportunidadeService, permissoes))
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
    }

    private UsernamePasswordAuthenticationToken logado(Usuario usuario) {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        return new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, List.of());
    }

    private void oportunidadeDoProf1() {
        when(oportunidadeRepository.findById("op-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").professorId("prof-1").build()));
    }

    @Test
    void alunoNaoCriaOportunidade() throws Exception {
        mvc.perform(post("/oportunidades").principal(logado(aluno))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"X\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(oportunidadeService);
    }

    @Test
    void professorCriaSempreEmNomeProprio() throws Exception {
        when(oportunidadeService.salvar(any(Oportunidade.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/oportunidades").principal(logado(professor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Monitoria\",\"professorId\":\"prof-2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professorId").value("prof-1"));

        ArgumentCaptor<Oportunidade> captor = ArgumentCaptor.forClass(Oportunidade.class);
        verify(oportunidadeService).salvar(captor.capture());
        assertEquals("prof-1", captor.getValue().getProfessorId());
    }

    @Test
    void validacaoDoServicoViraBadRequest() throws Exception {
        when(oportunidadeService.salvar(any(Oportunidade.class)))
                .thenThrow(new IllegalArgumentException("A oportunidade precisa ter ao menos 1 vaga."));

        mvc.perform(post("/oportunidades").principal(logado(professor))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A oportunidade precisa ter ao menos 1 vaga."));
    }

    @Test
    void alunoSeCandidata() throws Exception {
        when(oportunidadeService.candidatar("op-1", "aluno-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").build()));

        mvc.perform(post("/oportunidades/op-1/candidatar/aluno-1").principal(logado(aluno)))
                .andExpect(status().isOk());
    }

    @Test
    void alunoNaoCandidataOutroAluno() throws Exception {
        mvc.perform(post("/oportunidades/op-1/candidatar/aluno-2").principal(logado(aluno)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oportunidadeService);
    }

    @Test
    void inscricoesFechadasDa409() throws Exception {
        when(oportunidadeService.candidatar("op-1", "aluno-1"))
                .thenThrow(new IllegalStateException("As inscrições não estão abertas para esta oportunidade."));

        mvc.perform(post("/oportunidades/op-1/candidatar/aluno-1").principal(logado(aluno)))
                .andExpect(status().isConflict());
    }

    @Test
    void donoFinaliza() throws Exception {
        oportunidadeDoProf1();
        when(oportunidadeService.finalizar("op-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").finalizada(true).build()));

        mvc.perform(post("/oportunidades/op-1/finalizar").principal(logado(professor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizada").value(true));
    }

    @Test
    void outroProfessorNaoFinaliza() throws Exception {
        oportunidadeDoProf1();

        mvc.perform(post("/oportunidades/op-1/finalizar").principal(logado(outroProfessor)))
                .andExpect(status().isForbidden());

        verify(oportunidadeService, never()).finalizar(anyString());
    }

    @Test
    void alunoNaoAprova() throws Exception {
        mvc.perform(post("/oportunidades/op-1/aprovar/aluno-1").principal(logado(aluno)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oportunidadeService);
    }

    @Test
    void naoAprovaInformandoOutroProfessorNaUrl() throws Exception {
        mvc.perform(post("/oportunidades/op-1/aprovar/aluno-1/professor/prof-1").principal(logado(outroProfessor)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(oportunidadeService);
    }

    @Test
    void donoAprova() throws Exception {
        oportunidadeDoProf1();
        when(oportunidadeService.aprovarCandidatoDoProfessor("op-1", "aluno-1", "prof-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").vagasPreenchidas(1).build()));

        mvc.perform(post("/oportunidades/op-1/aprovar/aluno-1/professor/prof-1").principal(logado(professor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vagasPreenchidas").value(1));
    }

    @Test
    void oportunidadeInexistenteDa404() throws Exception {
        when(oportunidadeRepository.findById("op-x")).thenReturn(Optional.empty());

        mvc.perform(post("/oportunidades/op-x/finalizar").principal(logado(professor)))
                .andExpect(status().isNotFound());
    }
}
