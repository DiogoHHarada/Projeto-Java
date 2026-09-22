-- =====================================================================
-- Historico de transferencias
-- Execute no MySQL Workbench com "Execute all" (o raio amarelo).
-- =====================================================================

USE banco_digital;

-- ---------------------------------------------------------------------
-- PRE-REQUISITO: a tabela `contas` precisa ter PRIMARY KEY em `numero`,
-- senao o MySQL recusa criar as chaves estrangeiras abaixo com o erro
-- "Missing index for constraint ... in the referenced table 'contas'".
-- Rode estas linhas apenas se a sua tabela ainda nao tiver a PK
-- (confira com: SHOW CREATE TABLE contas;).
-- ---------------------------------------------------------------------
-- ALTER TABLE contas MODIFY numero  INT           NOT NULL;
-- ALTER TABLE contas MODIFY titular VARCHAR(100)  NOT NULL;
-- ALTER TABLE contas MODIFY saldo   DECIMAL(10,2) NOT NULL DEFAULT 0.00;
-- ALTER TABLE contas ADD PRIMARY KEY (numero);

CREATE TABLE IF NOT EXISTS transferencias (
    id                INT PRIMARY KEY AUTO_INCREMENT,
    conta_origem      INT           NOT NULL,
    conta_destino     INT           NOT NULL,
    valor             DECIMAL(10,2) NOT NULL,
    tarifa            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    modalidade_tarifa VARCHAR(30)   NOT NULL,
    data_hora         DATETIME      NOT NULL,
    CONSTRAINT fk_transf_origem  FOREIGN KEY (conta_origem)  REFERENCES contas(numero),
    CONSTRAINT fk_transf_destino FOREIGN KEY (conta_destino) REFERENCES contas(numero)
);

-- Conferir o historico:
SELECT * FROM transferencias ORDER BY data_hora DESC;

-- Total arrecadado em tarifas por modalidade:
SELECT modalidade_tarifa, COUNT(*) AS qtd, SUM(tarifa) AS total_tarifas
FROM transferencias
GROUP BY modalidade_tarifa;
