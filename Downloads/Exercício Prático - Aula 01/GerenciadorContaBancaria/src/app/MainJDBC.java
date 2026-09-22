package app;

import dao.ContaDAO;
import dao.TransferenciaDAO;
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
        TransferenciaDAO transferenciaDAO = new TransferenciaDAO();
        int numeroTeste = 9999;

        try {
            // ---------- 1. INSERIR ----------
            System.out.println("=== 1. INSERIR ===");
            if (dao.buscarPorNumero(numeroTeste) != null) {
                // apaga o historico antes: a chave estrangeira impede remover a conta
                transferenciaDAO.removerPorConta(numeroTeste);
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

            // ---------- 5. TRANSFERENCIA COM TRANSACAO (Aula 06) ----------
            System.out.println("\n=== 5. TRANSFERENCIA (transacao) ===");
            int destino = 9998;
            if (dao.buscarPorNumero(destino) != null) {
                transferenciaDAO.removerPorConta(destino);
                dao.remover(destino);
            }
            dao.inserir(new ContaCorrente(destino, "Conta Destino", 100.00));

            double origemAntes = dao.buscarPorNumero(numeroTeste).getSaldo();
            double destinoAntes = dao.buscarPorNumero(destino).getSaldo();
            System.out.printf("Antes:  origem=%.2f  destino=%.2f%n", origemAntes, destinoAntes);

            // 5a) transferencia valida -> COMMIT
            dao.transferir(numeroTeste, destino, 300.00);
            System.out.printf("Depois: origem=%.2f  destino=%.2f  (esperado %.2f e %.2f)%n",
                    dao.buscarPorNumero(numeroTeste).getSaldo(),
                    dao.buscarPorNumero(destino).getSaldo(),
                    origemAntes - 300.00, destinoAntes + 300.00);

            // 5b) saldo insuficiente -> ROLLBACK, nada muda
            double origemAgora = dao.buscarPorNumero(numeroTeste).getSaldo();
            double destinoAgora = dao.buscarPorNumero(destino).getSaldo();
            try {
                dao.transferir(numeroTeste, destino, 999999.00);
                System.out.println("ERRO: deveria ter lancado excecao");
            } catch (SaldoInsuficienteException e) {
                System.out.println("Saldo insuficiente barrado: " + e.getMessage());
                System.out.printf("   saldos apos rollback: origem=%.2f destino=%.2f  -> %s%n",
                        dao.buscarPorNumero(numeroTeste).getSaldo(),
                        dao.buscarPorNumero(destino).getSaldo(),
                        (dao.buscarPorNumero(numeroTeste).getSaldo() == origemAgora
                        && dao.buscarPorNumero(destino).getSaldo() == destinoAgora) ? "INTACTOS" : "MUDARAM!");
            }

            // 5c) destino inexistente -> o debito ja aconteceu e precisa ser DESFEITO
            try {
                dao.transferir(numeroTeste, 12345, 50.00);
                System.out.println("ERRO: deveria ter lancado excecao");
            } catch (SQLException e) {
                System.out.println("Destino inexistente barrado: " + e.getMessage());
                System.out.printf("   saldo da origem apos rollback: %.2f  -> %s%n",
                        dao.buscarPorNumero(numeroTeste).getSaldo(),
                        dao.buscarPorNumero(numeroTeste).getSaldo() == origemAgora
                        ? "INTACTO (rollback funcionou)" : "PERDEU DINHEIRO!");
            }

            // ---------- 6. REMOVER ----------
            System.out.println("\n=== 6. REMOVER ===");
            // o historico referencia as contas (chave estrangeira): apaga primeiro
            System.out.println("Historico de teste apagado: "
                    + (transferenciaDAO.removerPorConta(numeroTeste)
                    + transferenciaDAO.removerPorConta(destino)) + " linha(s)");
            System.out.println("Removida? " + dao.remover(numeroTeste));
            System.out.println("Busca apos remover: " + dao.buscarPorNumero(numeroTeste)
                    + "  (esperado null)");
            dao.remover(destino);

            System.out.println("\nTodos os testes JDBC executados com sucesso!");

        } catch (SQLException e) {
            System.err.println("ERRO DE BANCO DE DADOS: " + e.getMessage());
            System.err.println("Verifique: o MySQL esta ligado? o banco 'banco_digital' foi criado?"
                    + " DB_USER/DB_PASSWORD estao corretos?");
        } catch (SaldoInsuficienteException e) {
            System.err.println("Erro de saldo: " + e.getMessage());
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            // variaveis de ambiente do banco nao configuradas (Aula 06)
            Throwable causa = e.getCause();
            System.err.println("CONFIGURACAO AUSENTE: "
                    + (causa != null ? causa.getMessage() : e.toString()));
            System.err.println("Defina DB_URL, DB_USER e DB_PASSWORD."
                    + " Veja as instrucoes em config/DatabaseConfig.java.");
        }
    }
}
