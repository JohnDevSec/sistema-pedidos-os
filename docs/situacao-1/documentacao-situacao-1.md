# Documentação: Situação 1

Sistema de controle de pedidos e ordens de serviço. Back-end em Java (Spring Boot, MVC), banco PostgreSQL.

## 1. Escopo

O vendedor cria pedidos para clientes. O analista revisa, corrige e modifica o pedido antes de ele virar ordem de serviço (OS). Ao aprovar, o sistema gera a OS, que passa pelos status aberta, em andamento e concluída. Toda alteração feita em um pedido fica registrada em histórico.

Perfis de usuário: vendedor, analista e admin.

Funcionalidades: login, CRUD de clientes, CRUD de pedidos com itens, tela de revisão de pedidos, geração de ordem de serviço, histórico de alterações e gerenciamento de usuários.

Fluxo do pedido: `CRIADO` > `EM_REVISAO` > `APROVADO` (gera OS) ou `CANCELADO`.

## 2. Casos de uso

```mermaid
flowchart LR
    V([Vendedor])
    A([Analista])
    D([Admin])
    V --> UC1[Gerenciar clientes]
    V --> UC2[Criar pedido com itens]
    V --> UC3[Consultar pedidos]
    A --> UC4[Revisar e editar pedido]
    A --> UC5[Aprovar e gerar OS]
    A --> UC6[Atualizar status da OS]
    A --> UC7[Ver histórico]
    D --> UC8[Gerenciar usuários]
    D --> UC7
    V --> L[Login]
    A --> L
    D --> L
```

## 3. Diagrama de classes

```mermaid
classDiagram
    direction LR
    class Usuario {
        +Long id
        +String nome
        +String email
        +String senhaHash
        +Perfil perfil
        +boolean ativo
    }
    class Cliente {
        +Long id
        +String nome
        +String documento
        +String email
        +String telefone
    }
    class Pedido {
        +Long id
        +LocalDateTime dataCriacao
        +StatusPedido status
        +String observacao
        +BigDecimal valorTotal
    }
    class ItemPedido {
        +Long id
        +String descricao
        +int quantidade
        +BigDecimal valorUnitario
    }
    class OrdemServico {
        +Long id
        +LocalDateTime dataGeracao
        +StatusOS status
        +LocalDateTime dataConclusao
    }
    class HistoricoAlteracao {
        +Long id
        +LocalDateTime dataHora
        +String campo
        +String valorAnterior
        +String valorNovo
    }
    Cliente "1" --> "0..*" Pedido : faz
    Usuario "1" --> "0..*" Pedido : cria
    Pedido "1" *-- "1..*" ItemPedido : contem
    Pedido "1" --> "0..1" OrdemServico : gera
    Pedido "1" --> "0..*" HistoricoAlteracao : registra
    Usuario "1" --> "0..*" HistoricoAlteracao : altera
```

## 4. Modelo lógico (3FN)

```mermaid
erDiagram
    CLIENTE ||--o{ PEDIDO : faz
    USUARIO ||--o{ PEDIDO : cria
    PEDIDO ||--|{ ITEM_PEDIDO : contem
    PEDIDO ||--o| ORDEM_SERVICO : gera
    PEDIDO ||--o{ HISTORICO_ALTERACAO : registra
    USUARIO ||--o{ HISTORICO_ALTERACAO : altera
    USUARIO ||--o{ ORDEM_SERVICO : aprova
    USUARIO {
        bigint id PK
        varchar nome
        varchar email UK
        varchar senha_hash
        varchar perfil
        boolean ativo
    }
    CLIENTE {
        bigint id PK
        varchar nome
        varchar documento UK
        varchar email
        varchar telefone
    }
    PEDIDO {
        bigint id PK
        bigint cliente_id FK
        bigint vendedor_id FK
        timestamp data_criacao
        varchar status
        text observacao
        numeric valor_total
    }
    ITEM_PEDIDO {
        bigint id PK
        bigint pedido_id FK
        varchar descricao
        int quantidade
        numeric valor_unitario
    }
    ORDEM_SERVICO {
        bigint id PK
        bigint pedido_id FK
        bigint analista_id FK
        timestamp data_geracao
        varchar status
        timestamp data_conclusao
    }
    HISTORICO_ALTERACAO {
        bigint id PK
        bigint pedido_id FK
        bigint usuario_id FK
        timestamp data_hora
        varchar campo
        text valor_anterior
        text valor_novo
    }
```

O projeto físico está em [`database/schema.sql`](../../database/schema.sql) e o dicionário de dados em [`dicionario-de-dados.md`](dicionario-de-dados.md).

## 5. IHC: mapa de telas

```mermaid
flowchart TD
    Login --> Inicio
    Inicio --> Clientes
    Inicio --> Pedidos
    Inicio --> OS[Ordens de serviço]
    Inicio --> Usuarios
    Pedidos --> Revisar[Revisar pedido]
    Revisar --> Historico
```

Telas principais prototipadas: login, lista de pedidos, novo pedido e revisão do pedido (wireframes em `wireframes/`).

Princípios de IHC aplicados: menu igual em todas as telas (consistência), mensagens de erro que dizem o que fazer (feedback), ações de remover separadas das de salvar (reversibilidade) e histórico visível na revisão (o sistema comunica o que aconteceu).
