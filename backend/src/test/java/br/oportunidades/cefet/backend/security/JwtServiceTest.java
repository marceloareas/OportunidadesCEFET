package br.oportunidades.cefet.backend.security;

import br.oportunidades.cefet.backend.enums.FuncaoDeUsuario;
import br.oportunidades.cefet.backend.models.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-pelo-menos-trinta-e-dois-bytes!!";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-mais-de-trinta-e-dois-bytes";

    private final Usuario ana = Usuario.builder()
            .id("user-1").nome("Ana").email("ana@cefet.br").funcao(FuncaoDeUsuario.PROFESSOR).build();

    @Test
    void tokenGeradoCarregaEmailIdEFuncao() {
        JwtService jwt = new JwtService(SEGREDO, 60_000);

        String token = jwt.gerarToken(ana);

        assertEquals("ana@cefet.br", jwt.extrairEmail(token));
        assertEquals("user-1", jwt.extrairClaims(token).get("id"));
        assertEquals("PROFESSOR", jwt.extrairClaims(token).get("funcao"));
        assertTrue(jwt.tokenValido(token, ana));
    }

    @Test
    void tokenNaoValeParaOutroUsuario() {
        JwtService jwt = new JwtService(SEGREDO, 60_000);
        Usuario bia = Usuario.builder().id("user-2").email("bia@cefet.br").build();

        assertFalse(jwt.tokenValido(jwt.gerarToken(ana), bia));
    }

    @Test
    void tokenExpiradoEhRecusado() {
        JwtService jwt = new JwtService(SEGREDO, -1_000);

        String token = jwt.gerarToken(ana);

        assertThrows(ExpiredJwtException.class, () -> jwt.extrairEmail(token));
    }

    @Test
    void refreshTokenEhReconhecidoEApontaParaOUsuario() {
        JwtService jwt = new JwtService(SEGREDO, 60_000, 120_000);

        String refresh = jwt.gerarRefreshToken(ana);

        assertTrue(jwt.ehRefreshToken(refresh));
        assertEquals("ana@cefet.br", jwt.extrairEmail(refresh));
    }

    @Test
    void refreshTokenNaoServeComoTokenDeAcesso() {
        JwtService jwt = new JwtService(SEGREDO, 60_000);

        assertFalse(jwt.tokenValido(jwt.gerarRefreshToken(ana), ana));
        assertFalse(jwt.ehRefreshToken(jwt.gerarToken(ana)));
    }

    @Test
    void refreshTokenExpiradoOuAdulteradoNaoVale() {
        JwtService jwt = new JwtService(SEGREDO, 60_000, -1_000);

        assertFalse(jwt.ehRefreshToken(jwt.gerarRefreshToken(ana)));
        assertFalse(jwt.ehRefreshToken("nao.e.jwt"));
        assertFalse(jwt.ehRefreshToken(new JwtService(OUTRO_SEGREDO, 60_000).gerarRefreshToken(ana)));
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() {
        String token = new JwtService(OUTRO_SEGREDO, 60_000).gerarToken(ana);

        assertThrows(SignatureException.class, () -> new JwtService(SEGREDO, 60_000).extrairEmail(token));
    }
}
