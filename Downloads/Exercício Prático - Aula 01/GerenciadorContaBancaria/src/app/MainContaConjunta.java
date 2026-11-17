package app;

import concorrencia.SaqueTitular;
import model.ContaConjunta;

/**
 * Aula 09 - Controle de Concorrencia (atividade pratica).
 *
 * Cenario dos slides: conta conjunta com R$ 100,00 e os DOIS titulares
 * tentando sacar R$ 80,00 ao mesmo tempo. O correto e: um saque aprovado,
 * outro negado, saldo final R$ 20,00.
 *
 * O programa roda o mesmo cenario duas vezes:
 *   1) SEM sincronizacao  -> os dois saques passam e o saldo fica NEGATIVO
 *   2) COM synchronized   -> so um saque passa e o saldo fica correto
 * e no final mostra a comparacao.
 */
public class MainContaConjunta {

    private static final double SALDO_INICIAL = 100.00;
    private static final double VALOR_SAQUE = 80.00;

    public static void main(String[] args) throws InterruptedException {
        double[] semSync = executarCenario(false);
        double[] comSync = executarCenario(true);

        System.out.println();
        System.out.println("================ COMPARAÇÃO ================");
        System.out.printf("%-22s %-10s %-12s %s%n", "", "Saques OK", "Saldo final", "Consistente?");
        imprimirLinha("Sem sincronização", semSync);
        imprimirLinha("Com synchronized", comSync);
        System.out.printf("%-22s %-10d R$ %-9.2f%n", "Esperado", 1, SALDO_INICIAL - VALOR_SAQUE);
    }

    /**
     * Executa o cenario dos dois titulares sobre uma conta nova.
     * Devolve {quantidade de saques aprovados, saldo final}.
     */
    private static double[] executarCenario(boolean sincronizado) throws InterruptedException {
        System.out.println();
        System.out.println("===== " + (sincronizado ? "COM synchronized" : "SEM sincronização") + " =====");

        ContaConjunta conta = new ContaConjunta(2001, "Ana Ribeiro", "Pedro Lima", SALDO_INICIAL);
        System.out.println("Conta conjunta: " + conta);

        // dois titulares, duas threads, MESMA conta
        SaqueTitular ana = new SaqueTitular(conta, "Ana Ribeiro", VALOR_SAQUE, sincronizado);
        SaqueTitular pedro = new SaqueTitular(conta, "Pedro Lima", VALOR_SAQUE, sincronizado);

        Thread t1 = new Thread(ana, "Titular-1");
        Thread t2 = new Thread(pedro, "Titular-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        int aprovados = (ana.isAprovado() ? 1 : 0) + (pedro.isAprovado() ? 1 : 0);
        double saldoFinal = conta.getSaldo();

        double totalSacado = aprovados * VALOR_SAQUE;

        System.out.printf("Saques aprovados: %d (R$ %.2f entregues) | Saldo final: R$ %.2f%n",
                aprovados, totalSacado, saldoFinal);

        if (consistente(aprovados, saldoFinal)) {
            System.out.println(">> OK: a regra de saque foi respeitada.");
        } else if (saldoFinal < 0) {
            // as duas threads passaram pelo if antes de qualquer uma descontar
            System.out.println(">> INCONSISTÊNCIA: saldo NEGATIVO - o banco entregou R$ "
                    + String.format("%.2f", totalSacado) + " de uma conta com R$ "
                    + String.format("%.2f", SALDO_INICIAL) + ".");
        } else {
            // as duas threads leram o mesmo saldo em "saldo -= valor": um desconto se perdeu
            System.out.println(">> INCONSISTÊNCIA: ATUALIZAÇÃO PERDIDA - saíram R$ "
                    + String.format("%.2f", totalSacado) + ", mas o saldo só caiu R$ "
                    + String.format("%.2f", SALDO_INICIAL - saldoFinal) + ".");
        }

        return new double[]{aprovados, saldoFinal};
    }

    /**
     * Consistente = o saldo nunca fica negativo E o saldo final bate com o
     * dinheiro que realmente saiu (saldo inicial - saques aprovados).
     */
    private static boolean consistente(int aprovados, double saldoFinal) {
        double saldoCorreto = SALDO_INICIAL - aprovados * VALOR_SAQUE;
        return saldoFinal >= 0 && Math.abs(saldoFinal - saldoCorreto) < 0.001;
    }

    private static void imprimirLinha(String rotulo, double[] resultado) {
        boolean consistente = consistente((int) resultado[0], resultado[1]);
        System.out.printf("%-22s %-10d R$ %-9.2f %s%n",
                rotulo, (int) resultado[0], resultado[1], consistente ? "SIM" : "NÃO");
    }
}
