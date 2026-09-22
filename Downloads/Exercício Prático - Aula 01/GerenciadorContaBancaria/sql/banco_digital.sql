-- =====================================================================
-- Aula 05 - JDBC | Tarefa 1: Preparar o Banco de Dados
-- Abra este arquivo no MySQL Workbench e execute tudo (raio amarelo).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS banco_digital;
USE banco_digital;

CREATE TABLE IF NOT EXISTS contas (
    numero  INT PRIMARY KEY,
    titular VARCHAR(100)  NOT NULL,
    saldo   DECIMAL(10,2) NOT NULL DEFAULT 0.00
);

-- Contas iniciais (as mesmas do contas.txt).
-- INSERT IGNORE nao duplica se voce rodar o script mais de uma vez.
INSERT IGNORE INTO contas (numero, titular, saldo) VALUES
    (1001, 'João Silva',     1500.00),
    (1002, 'Maria Souza',    3200.50),
    (1003, 'Carlos Pereira',  780.25),
    (1004, 'Ana Ribeiro',    5000.00),
    (1005, 'Pedro Lima',     7500.00),
    (1006, 'Fernanda Costa', 15000.00);

SELECT * FROM contas;
