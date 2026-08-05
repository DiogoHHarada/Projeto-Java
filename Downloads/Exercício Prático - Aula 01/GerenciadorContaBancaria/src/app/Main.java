/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package app;

import model.ContaCorrente;
import service.ContaService;
import exception.SaldoInsuficienteException;

import javax.swing.*;
import java.io.IOException;

/**
 *
 * @author juliana
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ContaService cs = new ContaService();

        try {
            ContaCorrente conta = cs.lerConta("conta.txt");

            JOptionPane.showMessageDialog(null,
                "Conta carregada:\nNúmero: " + conta.getNumero() +
                "\nTitular: " + conta.getTitular() +
                "\nSaldo: R$ " + conta.getSaldo());

            String valorStr = JOptionPane.showInputDialog("Informe o valor para saque:");
            double valor = Double.parseDouble(valorStr);

            try {
                cs.sacarValor(conta, valor);
                JOptionPane.showMessageDialog(null, "Saque realizado com sucesso!");
            } catch (SaldoInsuficienteException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }

            cs.atualizarConta(conta, "conta_atualizada.txt");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao acessar arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
