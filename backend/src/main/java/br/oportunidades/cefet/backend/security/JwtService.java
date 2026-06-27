package br.oportunidades.cefet.backend.security;

import br.oportunidades.cefet.backend.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long expirationMs;

	public JwtService(
			@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.expiration-ms:86400000}") long expirationMs) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String gerarToken(Usuario usuario) {
		Date agora = new Date();
		return Jwts.builder()
				.subject(usuario.getEmail())
				.claim("id", usuario.getId())
				.claim("nome", usuario.getNome())
				.claim("funcao", usuario.getFuncao() != null ? usuario.getFuncao().name() : null)
				.issuedAt(agora)
				.expiration(new Date(agora.getTime() + expirationMs))
				.signWith(signingKey)
				.compact();
	}

	public String extrairEmail(String token) {
		return extrairClaims(token).getSubject();
	}

	public boolean tokenValido(String token, Usuario usuario) {
		Claims claims = extrairClaims(token);
		return usuario.getEmail().equals(claims.getSubject()) && claims.getExpiration().after(new Date());
	}

	public Claims extrairClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}