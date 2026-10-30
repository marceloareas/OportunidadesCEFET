package br.oportunidades.cefet.backend.security;

import br.oportunidades.cefet.backend.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	// Claim que separa o token de acesso (usado nas requisições) do refresh token
	// (usado só em /auth/refresh para pedir um novo par).
	static final String CLAIM_TIPO = "tipo";
	static final String TIPO_REFRESH = "refresh";

	private static final long SETE_DIAS_MS = 7L * 24 * 60 * 60 * 1000;

	private final SecretKey signingKey;
	private final long expirationMs;
	private final long refreshExpirationMs;

	@Autowired
	public JwtService(
			@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.expiration-ms:86400000}") long expirationMs,
			@Value("${security.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public JwtService(String secret, long expirationMs) {
		this(secret, expirationMs, SETE_DIAS_MS);
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

	public String gerarRefreshToken(Usuario usuario) {
		Date agora = new Date();
		return Jwts.builder()
				.subject(usuario.getEmail())
				.claim(CLAIM_TIPO, TIPO_REFRESH)
				.issuedAt(agora)
				.expiration(new Date(agora.getTime() + refreshExpirationMs))
				.signWith(signingKey)
				.compact();
	}

	// Verdadeiro só para refresh token válido (assinatura e validade conferidas).
	public boolean ehRefreshToken(String token) {
		try {
			return TIPO_REFRESH.equals(extrairClaims(token).get(CLAIM_TIPO, String.class));
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public String extrairEmail(String token) {
		return extrairClaims(token).getSubject();
	}

	// Refresh token não serve como token de acesso.
	public boolean tokenValido(String token, Usuario usuario) {
		Claims claims = extrairClaims(token);
		return usuario.getEmail().equals(claims.getSubject())
				&& claims.getExpiration().after(new Date())
				&& !TIPO_REFRESH.equals(claims.get(CLAIM_TIPO, String.class));
	}

	public Claims extrairClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
