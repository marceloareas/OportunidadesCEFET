package br.oportunidades.cefet.backend.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionControllerAdviceTest {

    private final ExceptionControllerAdvice advice = new ExceptionControllerAdvice();

    private ServletWebRequest requestFor(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        return new ServletWebRequest(req);
    }

    @Test
    void illegalStateExceptionRetorna409() {
        ResponseEntity<ErrorResponse> resp = advice.handleIllegalState(
                new IllegalStateException("Oportunidade já finalizada"), requestFor("/oportunidades/1/finalizar"));

        assertEquals(409, resp.getStatusCode().value());
        assertEquals("Oportunidade já finalizada", resp.getBody().message());
        assertEquals("/oportunidades/1/finalizar", resp.getBody().path());
    }

    @Test
    void securityExceptionRetorna403() {
        ResponseEntity<ErrorResponse> resp = advice.handleSecurity(
                new SecurityException("Sem permissão"), requestFor("/oportunidades/1"));

        assertEquals(403, resp.getStatusCode().value());
        assertEquals("Sem permissão", resp.getBody().message());
    }

    @Test
    void illegalArgumentExceptionRetorna400() {
        ResponseEntity<ErrorResponse> resp = advice.handleIllegalArgument(
                new IllegalArgumentException("Email já cadastrado"), requestFor("/users"));

        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Email já cadastrado", resp.getBody().message());
    }

    @Test
    void resourceNotFoundExceptionRetorna404() {
        ResponseEntity<ErrorResponse> resp = advice.handleNotFound(
                new ResourceNotFoundException("Usuário não encontrado"), requestFor("/users/1"));

        assertEquals(404, resp.getStatusCode().value());
        assertEquals("Usuário não encontrado", resp.getBody().message());
    }

    @Test
    void conflictExceptionRetorna409() {
        ResponseEntity<ErrorResponse> resp = advice.handleConflict(
                new ConflictException("Já existe um usuário cadastrado com esse e-mail."), requestFor("/users"));

        assertEquals(409, resp.getStatusCode().value());
        assertEquals("Já existe um usuário cadastrado com esse e-mail.", resp.getBody().message());
    }

    @Test
    void exceptionGenericaRetorna500SemExporMensagemInterna() {
        ResponseEntity<ErrorResponse> resp = advice.handleGeneric(
                new RuntimeException("stack trace sensível"), requestFor("/feed"));

        assertEquals(500, resp.getStatusCode().value());
        assertEquals("Erro interno no servidor.", resp.getBody().message());
    }
}
