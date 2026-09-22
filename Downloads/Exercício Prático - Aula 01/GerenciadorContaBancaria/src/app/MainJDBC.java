package app;

import dao.ContaDAO;
import exception.SaldoInsuficienteException;
import model.ContaCorrente;

import java.sql.SQLException;
import java.util.List;

/**
 * Main de teste do JDBC (Aula 05 - Tarefa 5).
 *
 * Testa, em ordem: inserir -> listar -> depositar -> sacar -> remover.
 * Use "Run File" (Shift+F6) com este arquivo aberto no NetBeans.
 */
public class MainJDBC {

    public static void main(String[] args) {
        ContaDAO dao = new ContaDAO();
        int numeroTeste = 9999;

        try {
            // ---------- 1. INSERIR ----------
            System.out.println("=== 1. INSERIR ===");
            if (dao.buscarPorNumero(numeroTeste) != null) {
                dao.remover(numeroTeste); // limpa execucao anterior
            }
            ContaCorrente nova = new ContaCorrente(numeroTeste, "Conta de Teste", 1000.00);
            dao.inserir(nova);
            System.out.println("Inserida: " + nova);

            // ---------- 2. LISTAR ----------
            System.out.println("\n=== 2. LISTAR ===");
            List<ContaCorrente> contas = dao.listar();
            contas.forEach(System.out::println);
            System.out.println("Total: " + contas.size() + " conta(s)");

            // ---------- 3. DEPOSITO ----------
            System.out.println("\n=== 3. DEPOSITO ===");
            ContaCorrente conta = dao.buscarPorNumero(numeroTeste);
            System.out.println("Saldo antes:  R$ " + conta.getSaldo());
            conta.depositar(500.00);                              // regra de negocio (memoria)
            dao.atualizarSaldo(conta.getNumero(), conta.getSaldo()); // persiste no banco
            System.out.println("Saldo depois: R$ " + dao.buscarPorNumero(numeroTeste).getSaldo()
                    + "  (esperado 1500.00)");

            // ---------- 4. SAQUE ----------
            System.out.println("\n=== 4. SAQUE ===");
            conta = dao.buscarPorNumero(numeroTeste);
            conta.sacar(200.00);
            dao.atualizarSaldo(conta.getNumero(), conta.getSaldo());
            System.out.println("Saldo depois: R$ " + dao.buscarPorNumero(numeroTeste).getSaldo()
                    + "  (esperado 1300.00)");

            // saque maior que o saldo -> excecao, banco nao muda
            try {
                conta = dao.buscarPorNumero(numeroTeste);
                conta.sacar(99999.00);
                dao.atualizarSaldo(conta.getNumero(), conta.getSaldo());
                System.out.println("ERRO: deveria ter lancado excecao");
            } catch (SaldoInsuficienteException e) {
                System.out.println("Saque invalido barrado: " + e.getMessage()
                        + " | saldo no banco: R$ " + dao.buscarPorNumero(numeroTeste).getSaldo());
            }

            // ---------- 5. REMOVER ----------
            System.out.println("\n=== 5. REMOVER ===");
            System.out.println("Removida? " + dao.remover(numeroTeste));
            System.out.println("Busca apos remover: " + dao.buscarPorNumero(numeroTeste)
                    + "  (esperado null)");

            System.out.println("\nTodos os testes JDBC executados com sucesso!");

        } catch (SQLException e) {
            System.err.println("ERRO DE BANCO DE DADOS: " + e.getMessage());
            System.err.println("Verifique: o MySQL esta ligado? o banco 'banco_digital' foi criado?"
                    + " usuario/senha em dao/Conexao.java estao corretos?");
        } catch (SaldoInsuficienteException e) {
            System.err.println("Erro de saldo: " + e.getMessage());
        }
    }
}
