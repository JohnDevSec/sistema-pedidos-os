# Sistema de Pedidos e Ordens de Serviço

Projeto Integrador Transdisciplinar em Engenharia de Software II (Cruzeiro do Sul Virtual).

Sistema web para controlar pedidos de vendedores. O analista revisa, corrige e modifica o pedido antes de ele virar ordem de serviço (OS), sem precisar entrar na fila de desenvolvimento de TI.

## Tecnologias

- Java 21, Spring Boot (MVC), Spring Security, Spring Data JPA
- Thymeleaf, HTML, CSS e JavaScript no front-end
- PostgreSQL 16
- JUnit 5 e Mockito nos testes
- Maven (wrapper incluso)

## Funcionalidades

- Login com três perfis: **vendedor**, **analista** e **admin**
- Cadastro de clientes (busca, paginação, documento único)
- Cadastro de usuários (somente admin, senha com BCrypt)
- Pedidos com vários itens e total calculado
- Revisão do pedido pelo analista, aprovação (gera a OS) e cancelamento
- Ordens de serviço com status aberta, em andamento e concluída
- Histórico de alterações de cada pedido

## Estrutura do repositório

```
sistema-pedidos-os/
├── README.md
├── app/               projeto Spring Boot (código e testes)
├── database/          schema.sql (projeto físico do banco)
└── docs/
    ├── situacao-1/    escopo, UML, modelo lógico, dicionário de dados, wireframes
    ├── situacao-2/    manual de uso
    └── situacao-3/    testes com usuários, formulários e correções
```

## Como rodar

### 1. Requisitos

- Java 21
- PostgreSQL 16 em execução
- `htpasswd` (pacote `apache2-utils`) para gerar o hash da senha do primeiro admin

### 2. Banco de dados

Crie o usuário e o banco (troque `SUA_SENHA`):

```
sudo -u postgres psql -c "CREATE USER pedidosos WITH PASSWORD 'SUA_SENHA';" -c "CREATE DATABASE pedidosos OWNER pedidosos;"
```

Crie as tabelas, na raiz do repositório:

```
psql -h localhost -U pedidosos -d pedidosos -f database/schema.sql
```

### 3. Primeiro usuário admin

```
read -s -p "Senha do admin: " ADMINPW; echo
HASH=$(htpasswd -bnBC 10 "" "$ADMINPW" | tr -d ':\n' | sed 's/^\$2y/\$2a/')
PGPASSWORD='SUA_SENHA' psql -h localhost -U pedidosos -d pedidosos -v hash="$HASH" <<'SQL'
INSERT INTO usuario (nome, email, senha_hash, perfil)
VALUES ('Administrador', 'admin@pedidos.local', :'hash', 'ADMIN');
SQL
unset ADMINPW HASH
```

Depois do primeiro acesso, os demais usuários (vendedor e analista) são criados pela tela **Usuários**.

### 4. Executar

A senha do banco é lida da variável de ambiente `DB_PASSWORD` e nunca fica no código:

```
cd app
export DB_PASSWORD='SUA_SENHA'
./mvnw spring-boot:run
```

Acesse http://localhost:8080 e entre com `admin@pedidos.local`.

Variáveis opcionais: `DB_USER` (padrão `pedidosos`).

### 5. Testes

```
cd app
export DB_PASSWORD='SUA_SENHA'
./mvnw test
```

A suíte tem testes unitários das regras de negócio de pedidos e ordens de serviço (JUnit e Mockito) e o teste que sobe a aplicação inteira contra o banco.

## Arquitetura

Padrão MVC em camadas: `controller` recebe a requisição e devolve a tela, `service` guarda as regras de negócio, `repository` acessa o banco e `model` contém as entidades JPA. As telas ficam em `app/src/main/resources/templates`.

## Documentação

- [Documentação da Situação 1](docs/situacao-1/documentacao-situacao-1.md)
- [Dicionário de dados](docs/situacao-1/dicionario-de-dados.md)
- [Manual de uso](docs/situacao-2/manual-de-uso.md)
