package br.oportunidades.cefet.backend.controllers;

import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import br.oportunidades.cefet.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
		return usuarioRepository.findByEmail(request.email())
				.filter(usuario -> usuario.getSenha() != null)
				// Só BCrypt: as senhas em texto puro são migradas na subida (SenhaLegadoBackfill).
				.filter(usuario -> request.senha() != null
						&& passwordEncoder.matches(request.senha(), usuario.getSenha()))
				.map(usuario -> ResponseEntity.ok(novaSessao(usuario)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	// Troca um refresh token válido por um novo par de tokens (B5). O refresh token também
	// é renovado, então quem usa o sistema com frequência não precisa logar de novo.
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
		if (request == null || request.refreshToken() == null || !jwtService.ehRefreshToken(request.refreshToken())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return usuarioRepository.findByEmail(jwtService.extrairEmail(request.refreshToken()))
				.map(usuario -> ResponseEntity.ok(novaSessao(usuario)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	private AuthResponse novaSessao(Usuario usuario) {
		return new AuthResponse(jwtService.gerarToken(usuario), jwtService.gerarRefreshToken(usuario), usuario);
	}

	@GetMapping("/me")
	public ResponseEntity<Usuario> me(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return usuarioRepository.findByEmail(authentication.getName())
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	public record LoginRequest(String email, String senha) {
	}

	public record RefreshRequest(String refreshToken) {
	}

	public record AuthResponse(String token, String refreshToken, Usuario usuario) {
	}
}