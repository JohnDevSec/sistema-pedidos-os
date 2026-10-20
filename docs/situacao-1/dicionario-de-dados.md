# Dicionário de dados

Sistema de controle de pedidos e ordens de serviço. SGBD: PostgreSQL. Script de criação: `schema.sql`.

Legenda: **PK** chave primária, **FK** chave estrangeira, **UK** valor único, **Obrig.** preenchimento obrigatório.

## usuario

Pessoas que acessam o sistema. O perfil define o que cada uma pode fazer.

| Coluna | Tipo | Obrig. | Chave | Regra / descrição |
|---|---|---|---|---|
| id | BIGINT | Sim | PK | Gerado automaticamente (identity) |
| nome | VARCHAR(120) | Sim | | Nome completo |
| email | VARCHAR(160) | Sim | UK | Único sem diferenciar maiúscula de minúscula; usado no login |
| senha_hash | VARCHAR(100) | Sim | | Senha criptografada (hash), nunca em texto puro |
| perfil | VARCHAR(20) | Sim | | VENDEDOR, ANALISTA ou ADMIN |
| ativo | BOOLEAN | Sim | | Padrão TRUE; usuário inativo não consegue logar |

## cliente

Clientes para os quais os pedidos são feitos.

| Coluna | Tipo | Obrig. | Chave | Regra / descrição |
|---|---|---|---|---|
| id | BIGINT | Sim | PK | Gerado automaticamente (identity) |
| nome | VARCHAR(120) | Sim | | Nome ou razão social |
| documento | VARCHAR(18) | Sim | UK | CPF ou CNPJ, único |
| email | VARCHAR(160) | Não | | Contato do cliente |
| telefone | VARCHAR(20) | Não | | Contato do cliente |

## pedido

Pedido criado pelo vendedor, revisado pelo analista antes de virar ordem de serviço.

| Coluna | Tipo | Obrig. | Chave | Regra / descrição |
|---|---|---|---|---|
| id | BIGINT | Sim | PK | Gerado automaticamente (identity) |
| cliente_id | BIGINT | Sim | FK | Referencia `cliente.id` |
| vendedor_id | BIGINT | Sim | FK | Referencia `usuario.id` (quem criou o pedido) |
| data_criacao | TIMESTAMP | Sim | | Padrão: data e hora atuais |
| status | VARCHAR(20) | Sim | | CRIADO, EM_REVISAO, APROVADO ou CANCELADO; padrão CRIADO |
| observacao | TEXT | Não | | Anotações livres sobre o pedido |
| valor_total | NUMERIC(12,2) | Sim | | Soma dos itens, calculada pelo back-end; maior ou igual a 0 |

## item_pedido

Itens que compõem um pedido. São apagados junto com o pedido.

| Coluna | Tipo | Obrig. | Chave | Regra / descrição |
|---|---|---|---|---|
| id | BIGINT | Sim | PK | Gerado automaticamente (identity) |
| pedido_id | BIGINT | Sim | FK | Referencia `pedido.id` (exclusão em cascata) |
| descricao | VARCHAR(200) | Sim | | Descrição do item ou serviço |
| quantidade | INTEGER | Sim | | Maior que 0 |
| valor_unitario | NUMERIC(12,2) | Sim | | Maior ou igual a 0 |

## ordem_servico

Gerada quando o analista aprova um pedido. Cada pedido gera no máximo uma ordem.

| Coluna | Tipo | Obrig. | Chave | Regra / descrição |
|---|---|---|---|---|
| id | BIGINT | Sim | PK | Gerado automaticamente (identity) |
| pedido_id | BIGINT | Sim | FK, UK | Referencia `pedido.id`; único (1 ordem por pedido) |
| analista_id | BIGINT | Sim | FK | Referencia `usuario.id` (quem aprovou) |
| data_geracao | TIMESTAMP | Sim | | Padrão: data e hora atuais |
| status | VARCHAR(20) | Sim | | ABERTA, EM_ANDAMENTO ou CONCLUIDA; padrão ABERTA |
| data_conclusao | TIMESTAMP | Não | | Preenchida ao concluir; não pode ser anterior a `data_geracao` |

## historico_alteracao

Registro de cada mudança feita em um pedido (quem, quando, o quê).

| Coluna | Tipo | Obrig. | Chave | Regra / descrição |
|---|---|---|---|---|
| id | BIGINT | Sim | PK | Gerado automaticamente (identity) |
| pedido_id | BIGINT | Sim | FK | Referencia `pedido.id` |
| usuario_id | BIGINT | Sim | FK | Referencia `usuario.id` (quem alterou) |
| data_hora | TIMESTAMP | Sim | | Padrão: data e hora atuais |
| campo | VARCHAR(60) | Sim | | Nome do campo alterado |
| valor_anterior | TEXT | Não | | Valor antes da alteração |
| valor_novo | TEXT | Não | | Valor depois da alteração |

## Relacionamentos

| Origem | Destino | Cardinalidade |
|---|---|---|
| cliente | pedido | 1 para N |
| usuario (vendedor) | pedido | 1 para N |
| pedido | item_pedido | 1 para N (mínimo 1 item) |
| pedido | ordem_servico | 1 para 0..1 |
| usuario (analista) | ordem_servico | 1 para N |
| pedido | historico_alteracao | 1 para N |
| usuario | historico_alteracao | 1 para N |

## Índices

| Tabela | Índice | Colunas |
|---|---|---|
| usuario | uq_usuario_email | LOWER(email) |
| pedido | ix_pedido_cliente | cliente_id |
| pedido | ix_pedido_vendedor | vendedor_id |
| pedido | ix_pedido_status | status |
| item_pedido | ix_item_pedido_pedido | pedido_id |
| ordem_servico | ix_os_analista | analista_id |
| ordem_servico | ix_os_status | status |
| historico_alteracao | ix_hist_pedido | pedido_id, data_hora |
| historico_alteracao | ix_hist_usuario | usuario_id |
