CREATE TABLE usuario (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('VENDEDOR', 'ANALISTA', 'ADMIN'))
);
CREATE UNIQUE INDEX uq_usuario_email ON usuario (LOWER(email));

CREATE TABLE cliente (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    documento   VARCHAR(18)  NOT NULL,
    email       VARCHAR(160),
    telefone    VARCHAR(20),
    CONSTRAINT uq_cliente_documento UNIQUE (documento)
);

CREATE TABLE pedido (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id    BIGINT        NOT NULL,
    vendedor_id   BIGINT        NOT NULL,
    data_criacao  TIMESTAMP     NOT NULL DEFAULT NOW(),
    status        VARCHAR(20)   NOT NULL DEFAULT 'CRIADO',
    observacao    TEXT,
    valor_total   NUMERIC(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_cliente  FOREIGN KEY (cliente_id)  REFERENCES cliente (id),
    CONSTRAINT fk_pedido_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuario (id),
    CONSTRAINT ck_pedido_status   CHECK (status IN ('CRIADO', 'EM_REVISAO', 'APROVADO', 'CANCELADO')),
    CONSTRAINT ck_pedido_valor    CHECK (valor_total >= 0)
);
CREATE INDEX ix_pedido_cliente  ON pedido (cliente_id);
CREATE INDEX ix_pedido_vendedor ON pedido (vendedor_id);
CREATE INDEX ix_pedido_status   ON pedido (status);

CREATE TABLE item_pedido (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id       BIGINT        NOT NULL,
    descricao       VARCHAR(200)  NOT NULL,
    quantidade      INTEGER       NOT NULL,
    valor_unitario  NUMERIC(12,2) NOT NULL,
    CONSTRAINT fk_item_pedido  FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT ck_item_qtd     CHECK (quantidade > 0),
    CONSTRAINT ck_item_valor   CHECK (valor_unitario >= 0)
);
CREATE INDEX ix_item_pedido_pedido ON item_pedido (pedido_id);

CREATE TABLE ordem_servico (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id       BIGINT      NOT NULL,
    analista_id     BIGINT      NOT NULL,
    data_geracao    TIMESTAMP   NOT NULL DEFAULT NOW(),
    status          VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    data_conclusao  TIMESTAMP,
    CONSTRAINT uq_os_pedido    UNIQUE (pedido_id),
    CONSTRAINT fk_os_pedido    FOREIGN KEY (pedido_id)   REFERENCES pedido (id),
    CONSTRAINT fk_os_analista  FOREIGN KEY (analista_id) REFERENCES usuario (id),
    CONSTRAINT ck_os_status    CHECK (status IN ('ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA')),
    CONSTRAINT ck_os_conclusao CHECK (data_conclusao IS NULL OR data_conclusao >= data_geracao)
);
CREATE INDEX ix_os_analista ON ordem_servico (analista_id);
CREATE INDEX ix_os_status   ON ordem_servico (status);

CREATE TABLE historico_alteracao (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id       BIGINT       NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    data_hora       TIMESTAMP    NOT NULL DEFAULT NOW(),
    campo           VARCHAR(60)  NOT NULL,
    valor_anterior  TEXT,
    valor_novo      TEXT,
    CONSTRAINT fk_hist_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedido (id),
    CONSTRAINT fk_hist_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
CREATE INDEX ix_hist_pedido  ON historico_alteracao (pedido_id, data_hora);
CREATE INDEX ix_hist_usuario ON historico_alteracao (usuario_id);
