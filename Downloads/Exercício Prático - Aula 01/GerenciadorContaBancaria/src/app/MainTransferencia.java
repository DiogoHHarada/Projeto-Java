package app;

import dao.ContaDAO;
import dao.TransferenciaDAO;
import exception.SaldoInsuficienteException;
import model.ContaCorrente;
import model.Transferencia;
import strategy.TarifaTransferenciaStrategy;

import java.sql.SQLException;
import java.util.List;

/**
 * Testes do historico de transferencias e das modalidades de tarifa.
 *
 * Use "Run File" (Shift+F6) com este arquivo aberto no NetBeans.
 * As contas de teste (7001 e 7002) sao criadas e removidas automaticamente,
 * entao pode rodar quantas vezes quiser.
 */
public class MainTransferencia {

    public static void main(String[] args) {
        ContaDAO contaDAO = new ContaDAO();
        TransferenciaDAO transferenciaDAO = new TransferenciaDAO();
        int origem = 7001;
        int destino = 7002;

        try {
            limpar(contaDAO, transferenciaDAO, origem, destino);
            contaDAO.inserir(new ContaCorrente(origem, "Origem Teste", 1000.00));
            contaDAO.inserir(new ContaCorrente(destino, "Destino Teste", 500.00));

            // ---------- 1. STRATEGY: cada modalidade calcula sua tarifa ----------
            System.out.println("=== 1. MODALIDADES DE TARIFA (valor R$ 200,00) ===");
            for (TarifaTransferenciaStrategy m : TarifaTransferenciaStrategy.values()) {
                System.out.printf("  %-11s -> R$ %6.2f   (%s)%n",
                        m.name(), m.calcularTarifa(200.00), m.getDescricao());
            }

            // ---------- 2. TRANSFERENCIA COM TARIFA ----------
            System.out.println("\n=== 2. TRANSFERENCIA COM TARIFA PERCENTUAL (1%) ===");
            Transferencia t = contaDAO.transferir(origem, destino, 200.00,
                    TarifaTransferenciaStrategy.PERCENTUAL);
            System.out.println("  registrada: " + t);
            System.out.printf("  origem:  %.2f  (esperado 798.00 = 1000 - 200 - 2)  -> %s%n",
                    contaDAO.buscarPorNumero(origem).getSaldo(),
                    confere(contaDAO.buscarPorNumero(origem).getSaldo(), 798.00));
            System.out.printf("  destino: %.2f  (esperado 700.00 = 500 + 200)       -> %s%n",
                    contaDAO.buscarPorNumero(destino).getSaldo(),
                    confere(contaDAO.buscarPorNumero(destino).getSaldo(), 700.00));

            // ---------- 3. OUTRAS MODALIDADES ----------
            System.out.println("\n=== 3. OUTRAS MODALIDADES ===");
            contaDAO.transferir(origem, destino, 100.00, TarifaTransferenciaStrategy.FIXA);
            System.out.printf("  FIXA   (R$ 5,00):    origem = %.2f  (esperado 693.00) -> %s%n",
                    contaDAO.buscarPorNumero(origem).getSaldo(),
                    confere(contaDAO.buscarPorNumero(origem).getSaldo(), 693.00));

            contaDAO.transferir(origem, destino, 100.00, TarifaTransferenciaStrategy.MISTA);
            System.out.printf("  MISTA  (2 + 0,5%%):   origem = %.2f  (esperado 590.50) -> %s%n",
                    contaDAO.buscarPorNumero(origem).getSaldo(),
                    confere(contaDAO.buscarPorNumero(origem).getSaldo(), 590.50));

            contaDAO.transferir(origem, destino, 100.00, TarifaTransferenciaStrategy.ISENTA);
            System.out.printf("  ISENTA (sem tarifa): origem = %.2f  (esperado 490.50) -> %s%n",
                    contaDAO.buscarPorNumero(origem).getSaldo(),
                    confere(contaDAO.buscarPorNumero(origem).getSaldo(), 490.50));

            // ---------- 4. ATOMICIDADE: falha nao pode gravar no historico ----------
            System.out.println("\n=== 4. ROLLBACK - DESTINO INEXISTENTE ===");
            int linhasAntes = transferenciaDAO.listar().size();
            double saldoAntes = contaDAO.buscarPorNumero(origem).getSaldo();
            try {
                contaDAO.transferir(origem, 99999, 50.00, TarifaTransferenciaStrategy.FIXA);
                System.out.println("  ERRO: deveria ter lancado excecao");
            } catch (SQLException e) {
                System.out.println("  barrado: " + e.getMessage());
                double saldoDepois = contaDAO.buscarPorNumero(origem).getSaldo();
                int linhasDepois = transferenciaDAO.listar().size();
                System.out.printf("  saldo da origem: %.2f -> %s%n",
                        saldoDepois, saldoDepois == saldoAntes ? "INTACTO" : "ERRADO");
                System.out.printf("  linhas no historico: %d -> %s%n",
                        linhasDepois,
                        linhasDepois == linhasAntes ? "NAO GRAVOU (correto)" : "GRAVOU (ERRADO)");
            }

            // ---------- 5. SALDO INSUFICIENTE CONSIDERANDO A TARIFA ----------
            System.out.println("\n=== 5. SALDO INSUFICIENTE POR CAUSA DA TARIFA ===");
            double saldoAtual = contaDAO.buscarPorNumero(origem).getSaldo();
            int linhasAntes5 = transferenciaDAO.listar().size();
            try {
                // transferir TODO o saldo com tarifa FIXA: falta o valor da tarifa
                contaDAO.transferir(origem, destino, saldoAtual, TarifaTransferenciaStrategy.FIXA);
                System.out.println("  ERRO: deveria ter lancado excecao");
            } catch (SaldoInsuficienteException e) {
                System.out.println("  barrado: " + e.getMessage());
                System.out.printf("  linhas no historico: %d -> %s%n",
                        transferenciaDAO.listar().size(),
                        transferenciaDAO.listar().size() == linhasAntes5
                                ? "NAO GRAVOU (correto)" : "GRAVOU (ERRADO)");
            }

            // ---------- 6. VALIDACOES ----------
            System.out.println("\n=== 6. VALIDACOES ===");
            testarInvalida(contaDAO, origem, origem, 10.00,
                    TarifaTransferenciaStrategy.ISENTA, "mesma conta   ");
            testarInvalida(contaDAO, origem, destino, -5.00,
                    TarifaTransferenciaStrategy.ISENTA, "valor negativo");
            testarInvalida(contaDAO, origem, destino, 10.00, null, "modalidade nula");

            // ---------- 7. CONSULTA AO HISTORICO ----------
            System.out.println("\n=== 7. HISTORICO DA CONTA " + origem + " ===");
            List<Transferencia> historico = transferenciaDAO.listarPorConta(origem);
            historico.forEach(x -> System.out.println("  " + x));
            System.out.printf("  %d transferencia(s) | transferido R$ %.2f | tarifas R$ %.2f%n",
                    historico.size(),
                    historico.stream().mapToDouble(Transferencia::getValor).sum(),
                    historico.stream().mapToDouble(Transferencia::getTarifa).sum());

            // ---------- limpeza ----------
            limpar(contaDAO, transferenciaDAO, origem, destino);
            System.out.println("\nTestes concluidos com sucesso!");

        } catch (SQLException e) {
            System.err.println("ERRO DE BANCO: " + e.getMessage());
            System.err.println("O MySQL esta ligado? A tabela de transferencias foi criada?"
                    + " (execute sql/transferencias.sql no Workbench)");
        } catch (SaldoInsuficienteException e) {
            System.err.println("Erro de saldo: " + e.getMessage());
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            Throwable causa = e.getCause();
            System.err.println("CONFIGURACAO AUSENTE: "
                    + (causa != null ? causa.getMessage() : e.toString()));
            System.err.println("Defina DB_URL, DB_USER e DB_PASSWORD."
                    + " Veja as instrucoes em config/DatabaseConfig.java.");
        }
    }

    private static String confere(double obtido, double esperado) {
        return Math.abs(obtido - esperado) < 0.001 ? "OK" : "FALHOU";
    }

    private static void testarInvalida(ContaDAO dao, int origem, int destino, double valor,
            TarifaTransferenciaStrategy modalidade, String caso) throws SQLException {
        try {
            dao.transferir(origem, destino, valor, modalidade);
            System.out.println("  " + caso + ": ERRO - deveria ter sido barrado");
        } catch (IllegalArgumentException e) {
            System.out.println("  " + caso + ": barrado -> " + e.getMessage());
        } catch (SaldoInsuficienteException e) {
            System.out.println("  " + caso + ": barrado -> " + e.getMessage());
        }
    }

    /** Apaga o historico das contas de teste antes de remove-las (chave estrangeira). */
    private static void limpar(ContaDAO contaDAO, TransferenciaDAO transferenciaDAO,
            int origem, int destino) throws SQLException {
        transferenciaDAO.removerPorConta(origem);
        transferenciaDAO.removerPorConta(destino);
        for (int n : new int[]{origem, destino}) {
            if (contaDAO.buscarPorNumero(n) != null) {
                contaDAO.remover(n);
            }
        }
    }
}
