# Relatório da 2ª Sprint — Oportunidades CEFET

**Equipe:** Ryan Calmon / Pedro Magalhães / Gustavo Arruda / Guilherme Delgado
**Período:** Setembro/2026
**Stack:** Java 21 · Spring Boot 3.5.6 · Spring Data MongoDB · Lombok · Angular 20.2 · Angular Material 20.2 · MongoDB 7 · Docker Compose

---

## 1. Escopo da Sprint

O tech lead definiu as seguintes atividades para a 2ª Sprint:

1. Mapear as dificuldades encontradas na configuração inicial (herdado da Sprint 1)
2. **I7** — CORS: remover código morto e limpezas relacionadas
3. **I8** — Problemas de fins de linha (line endings) entre sistemas operacionais
4. **I4** — Padronizar modais de confirmação
5. **I5** — Padronizar mensagens de sucesso e erro
6. **I6** — Handler global de exceções
7. **I1 (se houver tempo)** — Iniciar a parte de notificações

**Todos os itens foram entregues, incluindo o stretch de notificações.**

---

## 2. Resumo Executivo

| Item | Descrição | Status |
|------|-----------|--------|
| I7 | Remoção de `CorsConfig.java` morto + correção de warning Lombok + import não usado | ✅ Concluído |
| I8 | `.gitattributes` (LF em scripts) + `.env.example` | ✅ Concluído |
| I6 | `ExceptionControllerAdvice` global com respostas padronizadas | ✅ Concluído |
| I5 | `NotificationService` centralizado (toast/snackbar) substituindo 34 `alert()` | ✅ Concluído |
| I4 | `ConfirmDialogService` unificando 3 mecanismos de confirmação divergentes | ✅ Concluído |
| I1 | Base do sistema de notificações no backend (entidade, repositório, service) | ✅ Concluído (stretch) |

Cada entrega passou por revisão de conformidade (spec) e de qualidade de código, seguida de uma revisão final do conjunto completo e correção dos apontamentos.

---

## 3. Detalhamento das Entregas

### I7 — Limpeza de CORS e código morto

**Problema:** Havia dois arquivos configurando CORS: `backend/config/CorsConfig.java` (fora de `src/main/java/`, portanto **nunca compilado nem carregado** pelo Spring) e a configuração real no bean `corsConfigurationSource()` de `SecurityConfig.java`. A duplicidade gerava confusão.

**Feito:**
- Removido o `backend/config/CorsConfig.java` morto. A configuração ativa em `SecurityConfig.java` foi mantida intacta.
- Corrigido o warning do Lombok em `FeedItem.java`: o campo `createdAt = new Date()` recebeu `@Builder.Default`. Sem essa anotação, instâncias criadas via `builder()` recebiam `createdAt = null` em vez do valor padrão. Adicionado teste (`FeedItemTest`) validando o comportamento.
- Removido o import não utilizado de `RouterLink` em `oportunidades.ts` (import e array `imports` do componente).

### I8 — Fins de linha e onboarding

**Problema (herdado da Sprint 1):** No Windows, o `docker-entrypoint.sh` era salvo com CRLF, causando `exec: no such file or directory` no container Alpine Linux. Além disso, faltava um `.env.example` versionado para facilitar o setup de novos desenvolvedores.

**Feito:**
- Criado `.gitattributes` na raiz forçando `eol=lf` em `*.sh` (e `eol=crlf` em `*.cmd`/`*.bat`), prevenindo o problema de CRLF de forma definitiva em qualquer novo clone.
- Criado `.env.example` documentando todas as variáveis consumidas pelo `docker-compose.yml`, com valores placeholder (o `.env` real permanece fora do versionamento).

### I6 — Handler global de exceções

**Problema:** A classe `ExceptionControllerAdvice` existia mas estava **vazia**. O tratamento de erros estava espalhado em blocos `try/catch` ad-hoc nos controllers, com respostas inconsistentes (às vezes sem corpo, às vezes uma string crua). Não havia um formato de erro padronizado.

**Feito:**
- Implementado `ExceptionControllerAdvice` estendendo `ResponseEntityExceptionHandler`, com um DTO de erro único (`ErrorResponse`: `message`, `status`, `path`, `timestamp`).
- Criadas exceções de domínio `ConflictException` e `ResourceNotFoundException`.
- Mapeamento padronizado:

| Exceção | Status HTTP |
|---------|-------------|
| `MethodArgumentNotValidException` (validação) | 400 |
| `IllegalArgumentException` | 400 |
| `IllegalStateException` / `ConflictException` | 409 |
| `SecurityException` | 403 |
| `ResourceNotFoundException` / `NoSuchElementException` | 404 |
| Exceção genérica (não prevista) | 500 (sem vazar mensagem interna) |

- Removidos os `try/catch` redundantes de `UsuarioController`, `ComentarioController` e `OportunidadeController` — as exceções agora sobem para o handler global.
- **Decisão importante (semântica de status codes):** os controllers antigos mapeavam a *mesma* exceção para códigos diferentes. Padronizamos para o código semanticamente correto:
  - Duplicidade de e-mail (`UsuarioService`) agora lança `ConflictException` → mantém **409**.
  - Conflitos de estado ("oportunidade já finalizada", "vagas preenchidas", "não é possível comentar em oportunidade finalizada") passaram de 400 para **409** (Conflict é o código correto). Sem impacto de UX, pois o frontend exibe mensagens genéricas e não ramifica por status.
- A correção final garantiu que exceções do próprio Spring (JSON malformado, método HTTP não suportado, rota inexistente, tipo de parâmetro inválido) retornem seus códigos corretos (400/404/405) em vez de virarem 500.

### I5 — Feedback centralizado de sucesso/erro

**Problema:** O feedback ao usuário era feito com 34 chamadas de `alert()` nativo espalhadas por 7 arquivos, sem distinção visual entre sucesso e erro, com diálogos bloqueantes e sem estilo.

**Feito:**
- Criado `NotificationService` sobre o `MatSnackBar` do Angular Material, com `success(msg)` e `error(msg)` (cores e durações distintas por tipo).
- Substituídas as 34 chamadas de `alert()` nos 7 componentes, classificando cada mensagem como sucesso ou erro pelo seu texto.
- Adicionados os estilos globais dos toasts e o provider de animações (`provideAnimations()`), pré-requisito do Material.

### I4 — Modais de confirmação padronizados

**Problema:** Havia **três mecanismos de confirmação divergentes** na aplicação:
1. `confirm()` nativo do navegador (em `oportunidades.ts`, ação "finalizar")
2. Um modal inline controlado por signal em `post.ts` (ação "candidatar-se")
3. Outro modal inline controlado por signal em `candidatos-modal.ts` (ação "aprovar candidatos")

**Feito:**
- Criados `ConfirmDialogComponent` e `ConfirmDialogService` sobre o `MatDialog` do Angular Material, com uma API única: `confirm({ title, message, confirmLabel?, cancelLabel? }): Observable<boolean>`.
- Os três mecanismos foram unificados para usar o novo serviço. Todos os modais inline e o `confirm()` nativo foram removidos, junto com os signals e o CSS que os sustentavam.
- Todas as validações e o fluxo de controle originais foram preservados; fechar o diálogo pela lateral/ESC é corretamente tratado como "não confirmado".
- Adicionado tema pré-compilado do Material para o dialog renderizar corretamente.

### I1 — Base do sistema de notificações (stretch)

Entregue a fundação de persistência para a próxima sprint (sem endpoints ainda):
- Entidade `Notificacao` (`@Document("notificacoes")`), com `@Builder.Default` no timestamp desde o início.
- Enum `TipoNotificacao` (`CANDIDATURA_RECEBIDA`, `CANDIDATURA_APROVADA`, `OPORTUNIDADE_FINALIZADA`, `COMENTARIO_RECEBIDO`).
- `NotificacaoRepository` (`findByUsuarioIdOrderByCriadoEmDesc`, `countByUsuarioIdAndLidaFalse`).
- `NotificacaoService` (`criar`, `listarPorUsuario`, `contarNaoLidas`) — ponto de extensão que a próxima sprint chamará dos fluxos de negócio.

---

## 4. Dificuldades Encontradas e Resolvidas

Além das dificuldades já mapeadas na Sprint 1 (todas endereçadas por I6/I7/I8), foram encontradas e resolvidas durante o desenvolvimento:

- **Dependência `@angular/animations` ausente:** o `package.json` do frontend não declarava `@angular/animations`, necessário para `MatSnackBar`/`MatDialog`. Foi adicionada, alinhada à versão do `@angular/core`.
- **Ausência de tema do Angular Material:** o projeto usava Material sem nenhum tema configurado. Foi importado um tema pré-compilado (`azure-blue`) para os componentes de dialog/button/overlay renderizarem corretamente.
- **Ausência de `provideAnimations()`:** o `app.config.ts` não configurava animações, requisito dos componentes do Material.
- **Exceções de framework viravam 500:** identificado na revisão final — corrigido estendendo `ResponseEntityExceptionHandler`.

---

## 5. Situação dos Testes

Foram adicionados testes automatizados focados para as novas funcionalidades:

**Backend** (todos verdes):
- `FeedItemTest` — valida o `@Builder.Default`.
- `ExceptionControllerAdviceTest` — valida o mapeamento de cada exceção para o status/corpo corretos.
- `ExceptionControllerAdviceMvcTest` — valida (via MockMvc) que erros de framework retornam 4xx, não 500.
- `UsuarioServiceConflictTest` — valida que duplicidade de e-mail lança `ConflictException` (409).
- `NotificacaoServiceTest` — valida a criação de notificação.

**Frontend** (specs novos verdes):
- `notification.service.spec.ts` e `confirm-dialog.service.spec.ts`.

**Observações honestas sobre a suíte completa:**
- Backend: existe **1 falha pré-existente** (`BackendApplicationTests.contextLoads`) que exige uma instância real do MongoDB para subir o contexto Spring completo — não é regressão desta sprint (falha da mesma forma na base).
- Frontend: existem **6 falhas pré-existentes** nos testes auto-gerados "should create" de componentes, que não configuram o `HttpClient` no `TestBed`. Também não são regressões desta sprint (idênticas na base). Recomenda-se endereçá-las como parte de M2/M3 (testes) numa próxima sprint.

---

## 6. Arquivos Impactados

**Backend — criados:** `exceptions/ErrorResponse.java`, `exceptions/ConflictException.java`, `exceptions/ResourceNotFoundException.java`, `models/Notificacao.java`, `models/TipoNotificacao.java`, `repositories/NotificacaoRepository.java`, `services/NotificacaoService.java` + 5 classes de teste.
**Backend — modificados:** `exceptions/ExceptionControllerAdvice.java`, `models/FeedItem.java`, `services/UsuarioService.java`, `controllers/{Usuario,Comentario,Oportunidade}Controller.java`.
**Backend — removido:** `config/CorsConfig.java`.

**Frontend — criados:** `services/notification.service.ts` (+spec), `services/confirm-dialog.service.ts` (+spec), `components/confirm-dialog/` (component + template).
**Frontend — modificados:** `app.config.ts`, `styles.css`, `package.json`, e 7 componentes (`post`, `candidatos-modal`, `editar-perfil`, `perfil`, `login`, `oportunidades`, `home`) + limpeza de CSS morto.

**Raiz:** `.gitattributes`, `.env.example`.

---

## 7. Próximos Passos (para a próxima Sprint)

O sistema de notificações teve apenas sua **base** iniciada. Falta:
- **I2** — Frontend de notificações (componente na navbar, badge de não lidas, listagem).
- **I3** — Disparar notificações nos eventos de negócio (candidatura, aprovação, finalização, comentário) — chamando o `NotificacaoService.criar(...)` já disponível.
- `NotificacaoController` com os endpoints (listar, marcar como lida, contar não lidas).

Itens de média prioridade recomendados na sequência: **M2/M3** (testes automatizados de backend e frontend, incluindo corrigir as falhas pré-existentes de scaffolding) e **M1** (validação completa dos fluxos).

---

## 8. Como Verificar Localmente

**Backend (via Docker, sem JDK local):**
```bash
cd backend
docker run --rm -v "$(pwd)":/app -v "$HOME/.m2":/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn test
```

**Frontend:**
```bash
cd frontend
npm install
npx ng build --configuration development
npx ng test --watch=false
```

**Aplicação completa:**
```bash
docker compose up
```

---

*Documento de encerramento da 2ª Sprint. O planejamento detalhado da sprint está em `docs/superpowers/plans/2026-09-16-sprint-2.md`.*
