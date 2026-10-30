# Critérios de Aceite — Oportunidades e Candidaturas

Documento produzido na 4ª Sprint (M1 + M6). Descreve as regras de negócio **validadas** nos fluxos
de oportunidade e candidatura e o resultado esperado de cada cenário. Cada critério aponta o teste
automatizado que o garante.

Convenções: **Dado / Quando / Então**. "Dono" é o professor que criou a oportunidade.
Erros seguem o formato padronizado do `ExceptionControllerAdvice`
(`{ "message", "status", "path", "timestamp" }`).

---

## 1. Perfis e permissões

| Ação | Aluno | Professor (dono) | Professor (outro) | Resposta quando negado |
|---|---|---|---|---|
| Criar oportunidade | ✗ | ✓ | ✓ (vira dono) | 403 |
| Editar / excluir oportunidade | ✗ | ✓ | ✗ | 403 |
| Candidatar-se | ✓ (só em nome próprio) | ✗ | ✗ | 403 |
| Ver candidatos | ✗ | ✓ | ✗ | 403 |
| Aprovar candidato | ✗ | ✓ | ✗ | 403 |
| Finalizar oportunidade | ✗ | ✓ | ✗ | 403 |
| Curtir | ✓ (em nome próprio) | ✓ (em nome próprio) | ✓ (em nome próprio) | 403 |

- **CA-01** Dado um aluno logado, quando ele envia `POST /oportunidades`, então recebe **403** e nada é gravado.
  *Teste:* `OportunidadePermissoesTest.alunoNaoPodeCriarOportunidade`, `OportunidadeControllerTest`.
- **CA-02** Dado um professor logado, quando ele cria uma oportunidade informando outro `professorId`, então a
  oportunidade fica em nome **do professor logado** (o id vem do token, nunca do corpo).
  *Teste:* `OportunidadeControllerTest`.
- **CA-03** Dado um aluno logado, quando ele tenta `POST /oportunidades/{id}/candidatar/{outroAluno}`, então recebe **403**.
  *Teste:* `OportunidadePermissoesTest.alunoNaoCandidataOutroAluno`.
- **CA-04** Dado um professor que não é o dono, quando ele tenta aprovar, finalizar, editar ou ver candidatos, então recebe **403**.
  *Teste:* `OportunidadePermissoesTest.outroProfessorNaoGerencia`, `OportunidadeControllerTest`.
- **CA-05** Dada uma requisição sem token válido (ausente ou expirado) em qualquer rota fora de `/auth/**` e `POST /users`,
  então recebe **401**. O 403 fica reservado para recusa de permissão.

## 2. Criação de oportunidade

- **CA-06** Categoria obrigatória e válida (`MONITORIA`, `EXTENSAO`, `PESQUISA`, `ESTAGIO`, `ORIENTACAO_TCC`); caso contrário **400**.
- **CA-07** Pelo menos uma grande área do conhecimento; caso contrário **400**.
- **CA-08** Datas inicial e final de inscrição obrigatórias e `fim ≥ início`; caso contrário **400**.
- **CA-09** Pelo menos **1 vaga**; caso contrário **400** ("A oportunidade precisa ter ao menos 1 vaga.").
  *Teste:* `OportunidadeServiceRegrasTest.criarSemVagasEhRecusado`.
- **CA-10** Toda oportunidade nasce com `vagasPreenchidas = 0` e `finalizada = false`, mesmo que o cliente envie outros valores.
  *Teste:* `OportunidadeServiceRegrasTest.criarIgnoraVagasPreenchidasEFinalizadaDoCliente`.
- **CA-11** Ao criar, a oportunidade aparece no feed (`FeedItem` do tipo `OPORTUNIDADE`).

## 3. Status da oportunidade

| Situação | Status |
|---|---|
| `finalizada = true` | `FINALIZADA` |
| hoje < início das inscrições | `INSCRICOES_EM_BREVE` |
| hoje > fim das inscrições | `INSCRICOES_ENCERRADAS` |
| caso contrário | `INSCRICOES_ABERTAS` |

- **CA-12** O status é calculado a cada leitura (não é gravado), então muda sozinho quando o período começa ou termina.

## 4. Candidatura

- **CA-13** Só é possível se candidatar com status `INSCRICOES_ABERTAS`; caso contrário **409**.
  *Teste:* `OportunidadeServiceRegrasTest.candidaturaForaDoPeriodoEhRecusada`.
- **CA-14** Candidatar-se duas vezes não duplica a candidatura nem a notificação (operação idempotente).
  *Teste:* `OportunidadeServiceNotificacaoTest.candidaturaRepetidaNaoNotifica`.
- **CA-15** Nova candidatura nasce `CONCORRENDO` e o dono recebe a notificação `CANDIDATURA_RECEBIDA`.
- **CA-16** No frontend, o botão mostra "ENVIANDO..." e fica desabilitado durante o envio; depois vira "CANDIDATADO ✔".

## 5. Aprovação

- **CA-17** Só aprova quem está concorrendo naquela oportunidade; caso contrário **400**.
- **CA-18** Aprovar um aluno já aprovado devolve **409**.
- **CA-19** Ao aprovar: candidatura → `APROVADO`, `vagasPreenchidas + 1` e o aluno recebe `CANDIDATURA_APROVADA`.
- **CA-20** Quando `vagasPreenchidas` alcança `quantidadeDeVagas`, a oportunidade é **finalizada automaticamente** e todos os
  `CONCORRENDO` restantes passam para `RESERVA`, cada um com a notificação `OPORTUNIDADE_FINALIZADA`.

## 6. Finalização manual

- **CA-21** O dono pode finalizar antes de preencher todas as vagas pelo botão **"Finalizar oportunidade"** em
  *Oportunidades → Minhas oportunidades*, com modal de confirmação.
- **CA-22** Finalizar uma oportunidade já finalizada, ou com todas as vagas preenchidas, devolve **409**.
- **CA-23** Após finalizar: concorrentes → `RESERVA` (notificados), novas candidaturas e novos comentários são recusados.

## 7. Edição

- **CA-24** A edição nunca altera dono, `vagasPreenchidas` nem `finalizada` (esses campos só mudam pelos fluxos acima).
  *Teste:* `OportunidadeServiceRegrasTest.editarNaoTrocaDonoNemVagasPreenchidas`.
- **CA-25** A quantidade de vagas não pode ficar abaixo de 1 nem das vagas já preenchidas; caso contrário **400**.
  *Teste:* `OportunidadeServiceRegrasTest.editarNaoDeixaVagasAbaixoDasPreenchidas`.

## 8. Exibição para o aluno

- **CA-26** Em *Minhas candidaturas*, cada oportunidade mostra o status da candidatura do aluno
  (`Concorrendo`, `Aprovado`, `Reserva`), mais recentes primeiro e paginado.
  *Teste:* `OportunidadeServiceRegrasTest.candidaturasDoAlunoSaoPaginadas`.

## 9. Autenticação

- **CA-27** Login só aceita senha conferida por BCrypt. Senhas antigas em texto puro são convertidas automaticamente na
  subida do backend (`SenhaLegadoBackfill`), sem que o usuário perceba.
  *Teste:* `UsuarioServiceSenhaLegadoTest`, `AuthControllerTest`.
- **CA-28** O login devolve um token de acesso (24h) e um refresh token (7 dias). Quando o token de acesso expira, o
  frontend pede um novo par em `POST /auth/refresh` e repete a requisição, sem o usuário perceber. Se o refresh token
  também tiver expirado, a sessão é encerrada e o usuário volta ao login. O refresh token não vale como token de acesso.
  *Teste:* `JwtServiceTest`, `AuthControllerTest`, `auth.interceptor.spec`.

## 10. Perfis, publicações, comentários e itens salvos (sprint 5)

- **CA-29** Só o próprio usuário edita ou exclui o perfil; a função (aluno/professor) não muda pela edição (**403** ao
  tentar editar outro). *Teste:* `UsuarioControllerPermissaoTest`, `UsuarioServiceEdicaoTest`.
- **CA-30** Publicações e comentários ficam sempre em nome de quem está logado, mesmo que o corpo informe outro autor; só o
  autor apaga a publicação. *Teste:* `PostComentarioSalvosPermissaoTest`.
- **CA-31** Curtidas e itens salvos só podem ser feitos, removidos e listados em nome próprio (**403** caso contrário).
  *Teste:* `PostComentarioSalvosPermissaoTest`.
