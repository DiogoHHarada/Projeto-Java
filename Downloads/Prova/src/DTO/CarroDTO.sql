/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  diogo
 * Created: 19 de abr. de 2024
 */

CREATE TABLE tblCarro
(
    id SERIAL PRIMARY KEY,
    marca VARCHAR(255),
    placa VARCHAR(255),
    preco NUMERIC(255),
    cor VARCHAR(255),
);

