/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import exception.SaldoInsuficienteException;

/**
 *
 * @author juliana
 */
public abstract class Conta {
    protected int numero;
    protected String titular;
    protected double saldo;

    public Conta(int numero, String titular, double saldo) {
        this.numero = numero;
        this.titular = titular;
        this.saldo = saldo;
    }

    public abstract void sacar(double valor) throws SaldoInsuficienteException;

    public void depositar(double valor) {
        if (valor > 0) {
            saldo += valor;
        }
    }

    public void imprimirDados() {
        System.out.println("Conta nº: " + numero);
        System.out.println("Titular: " + titular);
        System.out.println("Saldo: R$ " + String.format("%.2f", saldo));
    }

    public int getNumero() { 
        return numero;
    }
    public String getTitular() {
        return titular;
    }
    public double getSaldo() { 
        return saldo;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    // usado por forEach(System.out::println) nos exemplos com Streams
    @Override
    public String toString() {
        return numero + " - " + titular + " - R$ " + String.format("%.2f", saldo);
    }

}
