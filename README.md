# FUSEx — Backend API

Backend do sistema de gestão integrada e auditoria de guias, atendimentos, espelhos e faturas médicas do **FUSEx (Fundo de Saúde do Exército)** em parceria com as Organizações Civis de Saúde (OCS/PSA), desenvolvido pelo time **DataSphere**.

Construído com **Spring Boot 3 / 4**, **Java 21**, **Maven**, **Flyway** e **PostgreSQL**.

---

## 📋 Pré-requisitos

- **Java 21** (JDK 21)
- **Maven wrapper** (`./mvnw`, já incluído no repositório)
- **Docker** e **Docker Compose**

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

A API ficará disponível em `http://localhost:8080`.

Para testar os cadastros e buscas da **US000-17**, consulte o [guia com mocks locais e requisições para o IntelliJ](docs/US000-17.md).

---

## 🐳 Stack Completa (PostgreSQL + Backend + Frontend)

Para subir todos os serviços integrados via Docker Compose, consulte as instruções no [README da raiz do projeto](../README.md).
