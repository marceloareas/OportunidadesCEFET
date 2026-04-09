package br.oportunidades.cefet.backend.security;

import br.oportunidades.cefet.backend.models.Usuario;
import br.oportunidades.cefet.backend.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UsuarioRepository usuarioRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
		this.jwtService = jwtService;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);
		String email;
		try {
			email = jwtService.extrairEmail(token);
		} catch (Exception ex) {
			filterChain.doFilter(request, response);
			return;
		}

		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			usuarioRepository.findByEmail(email)
					.filter(usuario -> jwtService.tokenValido(token, usuario))
					.ifPresent(usuario -> autenticarUsuario(request, usuario));
		}

		filterChain.doFilter(request, response);
	}

	private void autenticarUsuario(HttpServletRequest request, Usuario usuario) {
		List<SimpleGrantedAuthority> authorities = usuario.getFuncao() == null
				? List.of()
				: List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getFuncao().name()));

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				usuario.getEmail(),
			null,
			authorities
		);
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}