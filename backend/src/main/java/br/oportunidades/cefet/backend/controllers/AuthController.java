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
				.filter(usuario -> {
					boolean senhaHashValida = passwordEncoder.matches(request.senha(), usuario.getSenha());
					boolean senhaLegadaValida = request.senha().equals(usuario.getSenha());

					if (senhaLegadaValida && !senhaHashValida) {
						usuario.setSenha(passwordEncoder.encode(request.senha()));
						usuarioRepository.save(usuario);
					}

					return senhaHashValida || senhaLegadaValida;
				})
				.map(usuario -> ResponseEntity.ok(new AuthResponse(jwtService.gerarToken(usuario), usuario)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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

	public record AuthResponse(String token, Usuario usuario) {
	}
}