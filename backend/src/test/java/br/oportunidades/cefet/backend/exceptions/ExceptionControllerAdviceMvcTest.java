package br.oportunidades.cefet.backend.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova, via MockMvc standalone (sem contexto Spring / sem Mongo), que exceções lançadas pelo próprio
 * framework MVC (não pelos handlers de domínio) são mapeadas para o status 4xx correto pela superclasse
 * ResponseEntityExceptionHandler, e não caem no handleGeneric() -> 500.
 *
 * Exceções exercitadas:
 * - HttpMessageNotReadableException (JSON malformado no corpo do POST) -> 400
 * - MethodArgumentTypeMismatchException (path var "abc" onde se espera int) -> 400
 */
class ExceptionControllerAdviceMvcTest {

    private MockMvc mvc;

    @RestController
    static class DummyController {

        record DummyBody(String nome) {
        }

        @PostMapping("/dummy")
        public String criar(@RequestBody DummyBody body) {
            return "ok: " + body.nome();
        }

        @GetMapping("/dummy/{n}")
        public String buscar(@PathVariable int n) {
            return "ok: " + n;
        }
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
    }

    @Test
    void jsonMalformadoRetorna400NaoInterno() throws Exception {
        mvc.perform(post("/dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tipoIncompativelNoPathVarRetorna400NaoInterno() throws Exception {
        mvc.perform(get("/dummy/abc"))
                .andExpect(status().isBadRequest());
    }
}
