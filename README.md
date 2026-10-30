# 🌟 OportunidadesCEFET
Uma plataforma web para conectar alunos e professores do CEFET a projetos de pesquisa, extensão, monitoria e iniciação científica.
O sistema facilita a divulgação de oportunidades acadêmicas, além de permitir maior interação entre alunos e docentes.

# 🚨 Problema
- Muitos alunos têm interesse em participar de projetos, mas não sabem onde encontrar oportunidades.
- Professores enfrentam dificuldade para divulgar vagas, monitorar candidatos e organizar seleções.
- O processo atual é descentralizado (e-mails, cartazes, indicações pessoais), o que gera desinformação e baixa eficiência.

# 💡 Proposta
Criar uma plataforma estilo fórum, onde:
1. Professores podem publicar ideias ou andamento de projetos.
2. Alunos podem comentar, sugerir ideias ou se voluntariar para participar.
3. Professores decidem quem será aceito nos projetos.
4. Alunos cadastram suas experiências e áreas de interesse para aumentar visibilidade.

# 📌 Exemplo Prático
- ➡️ Um professor publica os objetivos de sua pesquisa.
- ➡️ Alunos e professores discutem o tema em comentários.
- ➡️ Alunos interessados podem se candidatar como voluntários.
- ➡️ O professor escolhe se aceita ou não o aluno para participar.

# 🛠️ Tecnologias
- Backend: Java 21, Spring Boot 3.5, Spring Security (JWT), MongoDB 7
- Frontend: Angular 20, Angular Material
- Infraestrutura: Docker Compose, nginx

# 🚀 Deploy no servidor

Pré-requisito: Docker com o plugin Compose (`docker compose version`). Nada mais precisa estar instalado.

```bash
git clone -b homolog https://github.com/marceloareas/OportunidadesCEFET.git
cd OportunidadesCEFET
cp .env.example .env
```

Edite o `.env`:

| Variável | O que é |
|---|---|
| `APP_PORT` | Porta em que o sistema fica no ar (`http://<servidor>:APP_PORT`). Padrão `8080`. |
| `MONGO_INITDB_ROOT_USERNAME` / `MONGO_INITDB_ROOT_PASSWORD` | Usuário e senha do MongoDB. Use só letras e números na senha. |
| `SECURITY_JWT_SECRET` | Segredo dos tokens de login, com no mínimo 32 caracteres (`openssl rand -base64 48`). |

Depois suba tudo:

```bash
docker compose up -d --build
docker compose ps        # mongo e backend devem aparecer como "healthy"
```

Só a porta `APP_PORT` fica aberta. O nginx entrega o site e repassa as chamadas `/api` para o backend; o
backend e o MongoDB ficam acessíveis apenas na rede interna do Docker. Os containers reiniciam sozinhos se
o servidor reiniciar.

**Trocar a porta:** altere `APP_PORT` no `.env` e rode `docker compose up -d` de novo.

**Atualizar para uma versão nova:**
```bash
git pull
docker compose up -d --build
```

**Ver os logs:** `docker compose logs -f backend` (ou `frontend`, `mongo`).

**Backup e restauração do banco** (os arquivos vão para `backups/`, que fica fora do git):
```bash
./backup-db.sh
./restore-db.sh backups/backup-oportunidades-AAAA-MM-DD_HHMM.gz
```

**Inspecionar o banco (opcional):** `docker compose --profile tools up -d mongo-express` abre o mongo-express
em `127.0.0.1:MONGO_EXPRESS_PORT` do próprio servidor (use um túnel SSH para acessar de fora).

> Os dados ficam no volume `mongo-data`. `docker compose down` mantém os dados; `docker compose down -v` **apaga** o banco.

# 💻 Desenvolvimento local

Com hot reload do Angular (`ng serve`) e o backend exposto para testes:

```bash
cp .env.example .env
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

- Frontend: `http://localhost:FRONTEND_PORT` (padrão 4200)
- API direto: `http://localhost:BACKEND_PORT` (padrão 8090)

Testes:
```bash
# backend (sem JDK local, via Docker)
cd backend
docker run --rm -v "$(pwd)":/app -v "$HOME/.m2":/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn test

# frontend
cd frontend
npm install
npx ng test --watch=false --browsers=ChromeHeadless
```

# 🤝 Contribuição
- Faça um fork do repositório
- Crie uma nova branch (git checkout -b feature/nova-funcionalidade)
- Commit suas alterações (git commit -m 'Adiciona nova funcionalidade')
- Push para a branch (git push origin feature/nova-funcionalidade)
- Abra um Pull Request
