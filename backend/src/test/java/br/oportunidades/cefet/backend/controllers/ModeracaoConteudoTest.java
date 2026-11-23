package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.exceptions.ResourceNotFoundException;
import br.oportunidades.cefet.backend.models.Oportunidade;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.OportunidadeRepository;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sprint 6: o administrador apaga comentários e postagens (discussões e oportunidades) de qualquer um.
@ExtendWith(MockitoExtension.class)
class ModeracaoConteudoTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OportunidadeRepository oportunidadeRepository;
    @Mock private PostService postService;
    @Mock private ComentarioService comentarioService;
    @Mock private OportunidadeService oportunidadeService;

    private MockMvc mvc;

    private final Usuario admin = Usuario.builder().id("adm").email("admin@cefet-rj.br")
            .funcao(FuncaoDeUsuario.ADMIN).build();
    private final Usuario bia = Usuario.builder().id("aluno-1").email("bia@cefet.br")
            .funcao(FuncaoDeUsuario.ALUNO).build();
    private final Usuario outroProf = Usuario.builder().id("prof-2").email("prof2@cefet.br")
            .funcao(FuncaoDeUsuario.PROFESSOR).build();

    private final UsernamePasswordAuthenticationToken comoAdmin =
            new UsernamePasswordAuthenticationToken("admin@cefet-rj.br", null, List.of());
    private final UsernamePasswordAuthenticationToken comoBia =
            new UsernamePasswordAuthenticationToken("bia@cefet.br", null, List.of());
    private final UsernamePasswordAuthenticationToken comoOutroProf =
            new UsernamePasswordAuthenticationToken("prof2@cefet.br", null, List.of());

    @BeforeEach
    void setup() {
        PermissoesUsuario permissoes = new PermissoesUsuario(usuarioRepository);
        OportunidadePermissoes permissoesOp = new OportunidadePermissoes(usuarioRepository, oportunidadeRepository);
        mvc = MockMvcBuilders
                .standaloneSetup(
                        new PostController(postService, permissoes),
                        new ComentarioController(comentarioService, permissoes),
                        new OportunidadeController(oportunidadeService, permissoesOp))
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
        lenient().when(usuarioRepository.findByEmail("admin@cefet-rj.br")).thenReturn(Optional.of(admin));
        lenient().when(usuarioRepository.findByEmail("bia@cefet.br")).thenReturn(Optional.of(bia));
        lenient().when(usuarioRepository.findByEmail("prof2@cefet.br")).thenReturn(Optional.of(outroProf));
        lenient().when(postService.buscarPorId("p1"))
                .thenReturn(Optional.of(Post.builder().id("p1").criadorId("prof-1").build()));
        lenient().when(oportunidadeRepository.findById("op-1"))
                .thenReturn(Optional.of(Oportunidade.builder().id("op-1").professorId("prof-1").build()));
    }

    @Test
    void adminApagaDiscussaoDeOutroUsuario() throws Exception {
        mvc.perform(delete("/posts/p1").principal(comoAdmin)).andExpect(status().isNoContent());
        verify(postService).deletar("p1");
    }

    @Test
    void adminApagaOportunidadeDeOutroProfessor() throws Exception {
        mvc.perform(delete("/oportunidades/op-1").principal(comoAdmin)).andExpect(status().isNoContent());
        verify(oportunidadeService).deletar("op-1");
    }

    @Test
    void professorQueNaoEhDonoContinuaSemApagarOportunidade() throws Exception {
        mvc.perform(delete("/oportunidades/op-1").principal(comoOutroProf)).andExpect(status().isForbidden());
        verify(oportunidadeService, never()).deletar(anyString());
    }

    @Test
    void adminApagandoOportunidadeInexistenteRecebe404() throws Exception {
        when(oportunidadeRepository.findById("nada")).thenReturn(Optional.empty());
        mvc.perform(delete("/oportunidades/nada").principal(comoAdmin)).andExpect(status().isNotFound());
        verify(oportunidadeService, never()).deletar(anyString());
    }

    @Test
    void adminApagaComentario() throws Exception {
        mvc.perform(delete("/comments/c1").principal(comoAdmin)).andExpect(status().isNoContent());
        verify(comentarioService).deletar("c1");
    }

    @Test
    void alunoNaoApagaComentario() throws Exception {
        mvc.perform(delete("/comments/c1").principal(comoBia)).andExpect(status().isForbidden());
        verify(comentarioService, never()).deletar(anyString());
    }

    @Test
    void comentarioInexistenteDa404() throws Exception {
        doThrow(new ResourceNotFoundException("Comentário não encontrado.")).when(comentarioService).deletar("x");
        mvc.perform(delete("/comments/x").principal(comoAdmin)).andExpect(status().isNotFound());
    }
}
