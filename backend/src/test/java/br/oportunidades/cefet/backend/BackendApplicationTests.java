package br.oportunidades.cefet.backend;

import br.oportunidades.cefet.backend.config.AvatarPadraoBackfill;
import br.oportunidades.cefet.backend.config.SenhaLegadoBackfill;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Sobe o contexto Spring inteiro sem precisar de MongoDB: o driver só conecta no primeiro
// acesso, então basta não criar índices na subida e não rodar os backfills (que leem o banco).
@SpringBootTest(properties = {
		"spring.data.mongodb.uri=mongodb://localhost:27017/oportunidades_teste?serverSelectionTimeoutMS=500",
		"spring.data.mongodb.auto-index-creation=false"
})
class BackendApplicationTests {

	@MockitoBean
	private AvatarPadraoBackfill avatarPadraoBackfill;

	@MockitoBean
	private SenhaLegadoBackfill senhaLegadoBackfill;

	@Test
	void contextLoads() {
	}

}
