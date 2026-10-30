# Planejamento de Atividades — Oportunidades CEFET
## 1ª Sprint — Agosto/Setembro 2026

---

## 1. Síntese do Estado Atual do Sistema

O sistema Oportunidades CEFET é uma plataforma fullstack (Java Spring Boot 3.5 + Angular 20 + MongoDB 7) que conecta alunos e professores do CEFET para oportunidades de pesquisa, extensão, monitoria e iniciação científica. Ao final da 6ª Sprint da equipe anterior, as funcionalidades centrais do módulo de Oportunidades estavam implementadas e estabilizadas.

**Stack tecnológica:**
- **Backend:** Java 21, Spring Boot 3.5.6, Spring Security (JWT), Spring Data MongoDB, Lombok
- **Frontend:** Angular 20.2, Angular Material 20.2, SSR, RxJS 7.8
- **Banco:** MongoDB 7
- **Infraestrutura:** Docker Compose (3 containers: mongo, backend, frontend)

---

## 2. Dificuldades Encontradas na Configuração Inicial

| # | Dificuldade | Descrição | Resolução |
|---|-------------|-----------|-----------|
| 1 | Arquivo `.env` ausente no repositório | O projeto depende de variáveis de ambiente (`.env`) que não são versionadas. Sem ele, o `docker compose up` falha silenciosamente ou com valores vazios. | Criado `.env` manualmente com os valores documentados no Manual do Desenvolvedor. |
| 2 | Line endings CRLF no `docker-entrypoint.sh` | Em ambientes Windows, o script `docker-entrypoint.sh` salva com CRLF, causando `exec: no such file or directory` no container Alpine Linux. | Convertido para LF. Recomendação: adicionar `.gitattributes` com `*.sh text eol=lf`. |
| 3 | Arquivo `CorsConfig.java` fora do classpath | O arquivo `backend/config/CorsConfig.java` está fora de `src/main/java/`, portanto não é compilado nem utilizado. A configuração CORS real está em `SecurityConfig.java`. | Arquivo pode ser removido para evitar confusão. |
| 4 | `package.json` raiz com dependência acidental | O `package.json` na raiz contém `{"dependencies": {"20": "^3.1.9"}}`, que parece ser uma entrada acidental/placeholder, não um manifesto real. | Sem impacto funcional, mas deveria ser corrigido ou removido. |
| 5 | Warning `@Builder` no `FeedItem.java` | Lombok emite warning sobre `@Builder` ignorando inicializador de campo. Deveria usar `@Builder.Default`. | Pendente de correção. |
| 6 | Warning `RouterLink` não utilizado em `OportunidadesPage` | Import de `RouterLink` presente mas não utilizado no template. | Pendente de remoção. |
| 7 | `ExceptionControllerAdvice` vazio | A classe de tratamento global de exceções está declarada mas sem nenhum handler implementado. Erros retornam respostas genéricas do Spring. | Pendente de implementação. |

---

## 3. Backlog de Atividades de Desenvolvimento

### 3.1 Atividades Imediatas (Alta Prioridade)

Estas são as atividades que devem ser realizadas nas primeiras sprints, com base nas pendências críticas documentadas pela equipe anterior e nos problemas identificados na análise do código.

| # | Atividade | Justificativa | Origem |
|---|-----------|---------------|--------|
| **I1** | **Implementar o Sistema de Notificações — Backend** | Principal funcionalidade pendente da 6ª Sprint. Criar entidade `Notificacao` no MongoDB, definir enum de tipos de evento (APROVACAO, CANDIDATURA, FINALIZACAO, COMENTARIO), implementar `NotificacaoRepository`, `NotificacaoService` e `NotificacaoController` com endpoints para listar, marcar como lida e contar não lidas. | Trabalhos Futuros #1 |
| **I2** | **Implementar o Sistema de Notificações — Frontend** | Criar componente de notificações na navbar, indicador visual (badge) de não lidas, página/modal de listagem de notificações, integração com o serviço de backend. | Trabalhos Futuros #1 |
| **I3** | **Disparar notificações nos eventos de negócio** | Integrar a criação de notificações nos fluxos existentes: aprovação de candidato, finalização de oportunidade, nova candidatura, novo comentário/resposta. | Trabalhos Futuros #1 |
| **I4** | **Padronizar modais de confirmação para ações críticas** | Ações como candidatar-se, aprovar candidatos, finalizar oportunidade e operações irreversíveis precisam de modais de confirmação consistentes. Atualmente há inconsistência e risco de sobreposição de modais. | Trabalhos Futuros #2 |
| **I5** | **Padronizar mensagens de sucesso e erro** | Criar um serviço/componente centralizado de feedback visual (toast/snackbar) para mensagens de sucesso, erro e validação, substituindo abordagens dispersas na aplicação. | Trabalhos Futuros #2 e #3 |
| **I6** | **Implementar `ExceptionControllerAdvice`** | O handler global de exceções está vazio. Implementar tratamento para exceções comuns (validação, não encontrado, não autorizado, conflito) com respostas padronizadas. | Análise de código |
| **I7** | **Corrigir `CorsConfig.java` morto e limpar código** | Remover o arquivo `CorsConfig.java` fora do classpath, corrigir warning do Lombok `@Builder`/`@Builder.Default` em `FeedItem.java`, remover import não utilizado em `OportunidadesPage`. | Análise de código |
| **I8** | **Adicionar `.gitattributes` e `.env.example`** | Prevenir problema de line endings em scripts shell no Windows. Criar `.env.example` versionado para facilitar onboarding. | Dificuldade encontrada |

---

### 3.2 Atividades de Média Prioridade

| # | Atividade | Justificativa | Origem |
|---|-----------|---------------|--------|
| **M1** | **Validação completa dos fluxos de oportunidade e candidatura** | A entidade Candidatura passou por mudanças relevantes ao longo das sprints. Validar fluxo completo de criação, inscrição, aprovação e finalização; exibição de status Aprovado; permissões por perfil. | Trabalhos Futuros #3 |
| **M2** | **Testes automatizados para endpoints críticos** | Não existem testes de integração/unitários significativos no backend (apenas classe de teste vazia). Criar testes para os fluxos de oportunidades, candidaturas, autenticação e feed. | Trabalhos Futuros #4 |
| **M3** | **Testes de interface para fluxos principais** | Criar testes de componente/E2E para fluxos de aluno e professor no frontend. | Trabalhos Futuros #4 |
| **M4** | **Revisar estados visuais de botões e elementos clicáveis** | Garantir `cursor: pointer`, estados `:hover`, `:disabled`, indicadores de carregamento (spinners) em todos os botões e elementos interativos. | Trabalhos Futuros #3 |
| **M5** | **Revisar segurança: migração de senhas legado** | O `AuthController.login()` aceita senhas em texto plano (legado) e migra para BCrypt no login. Avaliar se ainda existem usuários com senha legado e definir prazo para remover essa lógica. | Análise de código |
| **M6** | **Atualizar documentação técnica e critérios de aceite** | Atualizar manuais com o estado real do sistema, regras de negócio validadas e critérios de aceite atuais. | Trabalhos Futuros #5 |

---

### 3.3 Atividades de Baixa Prioridade (Evoluções Futuras)

| # | Atividade | Justificativa | Origem |
|---|-----------|---------------|--------|
| **B1** | Aprimorar filtros e busca de oportunidades | Adicionar mais critérios combinados de filtro, busca por texto livre. | Trabalhos Futuros #5 |
| **B2** | Melhorar página de portfólio | Adicionar campos complementares e melhorar organização visual. | Trabalhos Futuros #5 |
| **B3** | Adicionar histórico de interações do usuário | Registrar histórico de candidaturas, likes e discussões do usuário. | Trabalhos Futuros #5 |
| **B4** | Acessibilidade, responsividade e modo escuro | Avaliar conformidade com WCAG, testar responsividade em mobile, implementar tema escuro. | Trabalhos Futuros #5 |
| **B5** | Implementar refresh token JWT | Atualmente o sistema usa apenas access token com validade de 24h, sem refresh token. | Análise de código |
| **B6** | Componentizar `GroupComponent` | O componente `group` está como stub hardcoded. Avaliar se será utilizado ou removê-lo. | Análise de código |

---

## 4. Proposta de Foco para a Sprint Atual

Com base na priorização, a sprint atual deve focar em:

1. **Sistema de Notificações (I1, I2, I3)** — é a principal funcionalidade pendente e agrega valor direto ao usuário final.
2. **Padronização de UX (I4, I5)** — impacta diretamente a experiência e segurança do usuário.
3. **Correções técnicas imediatas (I6, I7, I8)** — baixo esforço, alto impacto na qualidade do código.

### Cronograma sugerido

| Semana | Foco | Entregáveis |
|--------|------|-------------|
| Semana 1 | Modelagem de notificações + correções técnicas | Entidade, repositório, endpoints de notificação. Correção de `CorsConfig`, warnings, `.gitattributes`, `.env.example`. |
| Semana 2 | Backend de notificações + integração com eventos | Serviço de notificação integrado aos fluxos de candidatura, aprovação, finalização e comentários. |
| Semana 3 | Frontend de notificações + padronização de UX | Componente de notificações, badge na navbar, modais de confirmação padronizados, serviço de toast/snackbar. |
| Semana 4 | Validação, testes e documentação | Validação E2E dos fluxos, testes para endpoints críticos, atualização de documentação. |

---

## 5. Mapeamento Técnico para o Sistema de Notificações

### Backend
- **Model:** `Notificacao` (id, usuarioId, tipo, mensagem, referenciaId, lida, criadoEm)
- **Enum:** `TipoNotificacao` (CANDIDATURA_RECEBIDA, CANDIDATURA_APROVADA, OPORTUNIDADE_FINALIZADA, COMENTARIO_RECEBIDO)
- **Repository:** `NotificacaoRepository` (findByUsuarioIdOrderByCriadoEmDesc, countByUsuarioIdAndLidaFalse)
- **Service:** `NotificacaoService` (criar, listar, marcarComoLida, contarNaoLidas)
- **Controller:** `NotificacaoController` (GET /notificacoes, PATCH /notificacoes/{id}/lida, GET /notificacoes/count)
- **Integração:** Injetar `NotificacaoService` em `OportunidadeService` (candidatar, aprovar, finalizar) e `ComentarioService` (criar)

### Frontend
- **Service:** `NotificacaoService` (listar, marcarLida, contarNaoLidas)
- **Componente:** `NotificacoesDropdown` na `NavbarRight` com badge de contagem
- **Polling:** Intervalo de atualização (ex: 30s) para contagem de não lidas

---

*Documento gerado em 28/08/2026 com base na análise do código-fonte, Manual do Desenvolvedor, Manual do Usuário e documento de Trabalhos Futuros.*
