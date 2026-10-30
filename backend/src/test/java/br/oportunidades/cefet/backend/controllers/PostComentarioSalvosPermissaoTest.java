package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.dto.item_salvo.SavedItemDTO;
import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.exceptions.ExceptionControllerAdvice;
import br.oportunidades.cefet.backend.models.Comentario;
import br.oportunidades.cefet.backend.models.Post;
import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.services.ComentarioService;
import br.oportunidades.cefet.backend.services.PermissoesUsuario;
import br.oportunidades.cefet.backend.services.PostService;
import br.oportunidades.cefet.backend.services.SavedItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sprint 5: posts, comentários e itens salvos sempre em nome de quem está logado.
@ExtendWith(MockitoExtension.class)
class PostComentarioSalvosPermissaoTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PostService postService;
    @Mock private ComentarioService comentarioService;
    @Mock private SavedItemService savedItemService;

    private MockMvc mvc;

    private final Usuario bia = Usuario.builder()
            .id("aluno-1").nome("Bia").email("bia@cefet.br").funcao(FuncaoDeUsuario.ALUNO).build();
    private final UsernamePasswordAuthenticationToken comoBia =
            new UsernamePasswordAuthenticationToken("bia@cefet.br", null, List.of());

    @BeforeEach
    void setup() {
        PermissoesUsuario permissoes = new PermissoesUsuario(usuarioRepository);
        mvc = MockMvcBuilders
                .standaloneSetup(
                        new PostController(postService, permissoes),
                        new ComentarioController(comentarioService, permissoes),
                        new SavedItemController(savedItemService, permissoes))
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
        lenient().when(usuarioRepository.findByEmail("bia@cefet.br")).thenReturn(Optional.of(bia));
    }

    @Test
    void postFicaEmNomeDeQuemEstaLogado() throws Exception {
        when(postService.salvar(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/posts").principal(comoBia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Oi\",\"criadorId\":\"prof-1\",\"nomeCriador\":\"Outro\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postService).salvar(captor.capture());
        assertEquals("aluno-1", captor.getValue().getCriadorId());
        assertEquals("Bia", captor.getValue().getNomeCriador());
    }

    @Test
    void naoApagaPostDeOutro() throws Exception {
        when(postService.buscarPorId("p1"))
                .thenReturn(Optional.of(Post.builder().id("p1").criadorId("prof-1").build()));

        mvc.perform(delete("/posts/p1").principal(comoBia)).andExpect(status().isForbidden());

        verify(postService, never()).deletar(anyString());
    }

    @Test
    void autorApagaOProprioPost() throws Exception {
        when(postService.buscarPorId("p1"))
                .thenReturn(Optional.of(Post.builder().id("p1").criadorId("aluno-1").build()));

        mvc.perform(delete("/posts/p1").principal(comoBia)).andExpect(status().isNoContent());

        verify(postService).deletar("p1");
    }

    @Test
    void apagarPostInexistenteDa404() throws Exception {
        when(postService.buscarPorId("x")).thenReturn(Optional.empty());

        mvc.perform(delete("/posts/x").principal(comoBia)).andExpect(status().isNotFound());
    }

    @Test
    void naoCurtePostEmNomeDeOutro() throws Exception {
        mvc.perform(post("/posts/p1/like/prof-1").principal(comoBia)).andExpect(status().isForbidden());

        verify(postService, never()).alternarLike(anyString(), anyString());
    }

    @Test
    void comentarioFicaEmNomeDeQuemEstaLogado() throws Exception {
        when(comentarioService.salvar(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/comments").principal(comoBia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Legal\",\"usuarioId\":\"prof-1\",\"idPost\":\"p1\",\"tipoEntidadePai\":\"POST\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
        verify(comentarioService).salvar(captor.capture());
        assertEquals("aluno-1", captor.getValue().getUsuarioId());
    }

    @Test
    void salvaItemSoEmNomeProprio() throws Exception {
        mvc.perform(post("/saved-items").principal(comoBia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"aluno-1\",\"feedItemId\":\"op-1\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/saved-items").principal(comoBia).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"prof-1\",\"feedItemId\":\"op-1\"}"))
                .andExpect(status().isForbidden());

        verify(savedItemService, times(1)).salvar(any(SavedItemDTO.class));
    }

    @Test
    void naoVeNemRemoveItensSalvosDeOutro() throws Exception {
        mvc.perform(get("/saved-items/user/prof-1").principal(comoBia)).andExpect(status().isForbidden());
        mvc.perform(delete("/saved-items/prof-1/op-1").principal(comoBia)).andExpect(status().isForbidden());

        verifyNoInteractions(savedItemService);
    }

    @Test
    void veOsPropriosItensSalvos() throws Exception {
        when(savedItemService.listar(eq("aluno-1"), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mvc.perform(get("/saved-items/user/aluno-1").principal(comoBia)).andExpect(status().isOk());
    }
}
