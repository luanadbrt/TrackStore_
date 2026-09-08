-- =============================================================================
-- Track Store - criação completa do banco de dados
-- =============================================================================
--
-- Este script cria o schema INTEIRO. A aplicação não cria mais tabelas sozinha:
-- hibernate.cfg.xml usa hbm2ddl.auto=validate, que apenas CONFERE se o banco bate
-- com as entidades e falha na inicialização se estiver divergente.
--
-- (A versão anterior usava hbm2ddl.auto=update, que só adiciona colunas: nunca
-- altera tipo, nunca corrige constraint. A divergência ficava escondida até virar
-- dado corrompido.)
--
-- Como usar:
--   mysql -u root -p < database/script.sql
-- ou abra o arquivo no MySQL Workbench e execute tudo.
-- =============================================================================

DROP DATABASE IF EXISTS trackstore;
CREATE DATABASE trackstore
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE trackstore;

-- -----------------------------------------------------------------------------
-- usuarios
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
                          id          INT          NOT NULL AUTO_INCREMENT,
                          nome        VARCHAR(150) NOT NULL,
                          email       VARCHAR(150) NOT NULL,
    -- CPF: 11 dígitos, sem pontuação, como TEXTO para não perder zeros à esquerda.
    -- Os dígitos verificadores são conferidos pela aplicação (CpfUtil).
                          cpf         VARCHAR(11)  NOT NULL,
    -- senha nunca em texto puro: PBKDF2-HMAC-SHA256 com salt por usuário
                          senha_hash  VARCHAR(255) NOT NULL,
                          senha_salt  VARCHAR(64)  NOT NULL,
                          tipo        VARCHAR(20)  NOT NULL,
                          PRIMARY KEY (id),
                          CONSTRAINT uk_usuarios_email UNIQUE (email),
                          CONSTRAINT uk_usuarios_cpf   UNIQUE (cpf)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- fornecedores
-- -----------------------------------------------------------------------------
CREATE TABLE fornecedores (
                              id   INT          NOT NULL AUTO_INCREMENT,
                              nome VARCHAR(150) NOT NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT uk_fornecedores_nome UNIQUE (nome)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- produtos
-- -----------------------------------------------------------------------------
CREATE TABLE produtos (
                          id         INT           NOT NULL AUTO_INCREMENT,
                          codigo     INT           NOT NULL,
                          nome       VARCHAR(150)  NOT NULL,
    -- INT, não DOUBLE: não se vende meia unidade
                          quantidade INT           NOT NULL,
    -- DECIMAL, não DOUBLE: ponto flutuante não representa 0,10 exatamente
                          preco      DECIMAL(10,2) NOT NULL,
    -- ATIVO / INATIVO: exclusão de produto é lógica, para preservar o histórico
                          situacao   VARCHAR(10)   NOT NULL DEFAULT 'ATIVO',
                          PRIMARY KEY (id),
                          CONSTRAINT uk_produtos_codigo UNIQUE (codigo),
                          CONSTRAINT uk_produtos_nome   UNIQUE (nome),
                          CONSTRAINT ck_produtos_quantidade CHECK (quantidade >= 0),
                          CONSTRAINT ck_produtos_preco      CHECK (preco > 0)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- pedidos
-- -----------------------------------------------------------------------------
CREATE TABLE pedidos (
                         id          INT           NOT NULL AUTO_INCREMENT,
                         usuario_id  INT           NOT NULL,
                         total       DECIMAL(10,2) NOT NULL,
                         data_pedido DATETIME(6)   NOT NULL,
                         PRIMARY KEY (id),
                         CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE = InnoDB;

CREATE INDEX ix_pedidos_data ON pedidos (data_pedido);

-- -----------------------------------------------------------------------------
-- itens_pedido
-- -----------------------------------------------------------------------------
CREATE TABLE itens_pedido (
                              id             INT           NOT NULL AUTO_INCREMENT,
                              pedido_id      INT           NOT NULL,
                              produto_id     INT           NOT NULL,
                              quantidade     INT           NOT NULL,
    -- preço congelado no momento da compra: se o produto mudar de preço depois,
    -- o pedido antigo continua mostrando o que o cliente realmente pagou
                              preco_unitario DECIMAL(10,2) NOT NULL,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_itens_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedidos (id),
                              CONSTRAINT fk_itens_produto FOREIGN KEY (produto_id) REFERENCES produtos (id),
                              CONSTRAINT ck_itens_quantidade CHECK (quantidade > 0)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------------
-- reposicoes
-- -----------------------------------------------------------------------------
CREATE TABLE reposicoes (
                            id             INT         NOT NULL AUTO_INCREMENT,
                            produto_id     INT         NOT NULL,
                            fornecedor_id  INT         NOT NULL,
                            quantidade     INT         NOT NULL,
                            data_reposicao DATETIME(6) NOT NULL,
                            PRIMARY KEY (id),
                            CONSTRAINT fk_reposicoes_produto    FOREIGN KEY (produto_id)    REFERENCES produtos (id),
                            CONSTRAINT fk_reposicoes_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedores (id),
    -- rede de segurança contra reposição negativa, além da validação da aplicação
                            CONSTRAINT ck_reposicoes_quantidade CHECK (quantidade > 0)
) ENGINE = InnoDB;

CREATE INDEX ix_reposicoes_data ON reposicoes (data_reposicao);

-- =============================================================================
-- Dados iniciais
-- =============================================================================

INSERT INTO fornecedores (nome) VALUES
                                    ('Fornecedor Central'),
                                    ('Distribuidora Boas Novas'),
                                    ('Atacado Avivamento');

INSERT INTO produtos (codigo, nome, quantidade, preco, situacao) VALUES
                                                                     (1001, 'Bíblia de Estudo',        25, 89.90,  'ATIVO'),
                                                                     (1002, 'Caderno de Anotações',    60, 12.50,  'ATIVO'),
                                                                     (1003, 'Caneta Esferográfica',   200,  3.00,  'ATIVO'),
                                                                     (1004, 'Camiseta do Avivamento',  40, 45.00,  'ATIVO'),
                                                                     (1005, 'Caneca Personalizada',    15, 29.90,  'ATIVO');

-- =============================================================================
-- Usuário do banco para a aplicação
-- =============================================================================
--
-- NÃO use root na aplicação. Descomente as duas linhas abaixo, troque a senha e
-- use esses dados no db.properties (modelo em db.properties.example).
--
-- CREATE USER IF NOT EXISTS 'trackstore'@'localhost' IDENTIFIED BY 'troque-esta-senha';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON trackstore.* TO 'trackstore'@'localhost';
-- FLUSH PRIVILEGES;

-- =============================================================================
-- Primeiro acesso
-- =============================================================================
--
-- Não é mais preciso promover ninguém a admin na mão: o PRIMEIRO usuário
-- cadastrado pela tela de login vira ADMIN automaticamente (UsuarioService).
-- Os seguintes viram CLIENTE, e o admin pode mudar o tipo de qualquer um na tela
-- "Gestão de Usuários".
--
-- Se ainda assim precisar promover alguém pelo banco:
-- UPDATE usuarios SET tipo = 'ADMIN' WHERE email = 'seu-email@exemplo.com';
