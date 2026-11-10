# FUSEx — Backend API

Backend do sistema de gestão integrada e auditoria de guias, atendimentos, espelhos e faturas médicas do **FUSEx (Fundo de Saúde do Exército)** em parceria com as Organizações Civis de Saúde (OCS/PSA), desenvolvido pelo time **DataSphere**.

Construído com **Spring Boot 3 / 4**, **Java 21**, **Maven**, **Flyway** e **PostgreSQL**.

---

## 📋 Pré-requisitos

- **Java 21** (JDK 21)
- **Maven wrapper** (`./mvnw`, já incluído no repositório)
- **Docker** e **Docker Compose**
- **Node.js** `>= 22.18` (caso execute o frontend localmente)

---

## ⚙️ Variáveis de Ambiente

Crie seu arquivo `.env` a partir do modelo de exemplo:

```bash
cp .env.example .env
```

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `DB_NAME` | Nome do banco de dados PostgreSQL | `fusex-datasphere` |
| `DB_USERNAME` | Nome de usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha de acesso ao banco | `admin` |

---

## 🏷️ Perfis da Aplicação (`dev`)

- **Padrão (`default`)**: Carrega apenas migrações estruturais do banco de dados a partir de `classpath:db/migration`.
- **Desenvolvimento (`dev`)**: Carrega adicionalmente as migrações e sementes de dados de teste localizadas em `classpath:db/dev` (`spring.flyway.locations=classpath:db/migration,classpath:db/dev`).

---

## 🐳 Como Subir com Docker (Stack Completa)

Para clonar e inicializar a stack inteira (PostgreSQL, Backend e Frontend):

```bash
# 1. Clonar incluindo os submódulos
git clone --recurse-submodules https://github.com/DataSphere-API/API_3_SEM_BACKEND.git
cd API_3_SEM_BACKEND

# 2. Configurar o arquivo de variáveis de ambiente
cp .env.example .env

# 3. Subir os serviços com build
docker compose up --build
```

### URLs da Aplicação

- **Backend API**: `http://localhost:8080`
- **Frontend Web**: `http://localhost:5173`

---

## 💻 Como Rodar Localmente (Desenvolvimento)

Para executar a API na sua máquina com o banco PostgreSQL no Docker:

```bash
# 1. Subir apenas o banco PostgreSQL isolado
docker compose -f compose.dev.yaml up -d

# 2. Executar a aplicação com o Maven wrapper
./mvnw spring-boot:run

# Para rodar com o perfil de desenvolvimento (com dados de teste):
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
