package concorrencia;

import model.ContaConjunta;

/**
 * Aula 09 - tarefa executada por cada titular da conta conjunta.
 *
 * Cada titular roda em uma thread propria e tenta sacar da MESMA conta.
 * O parametro `sincronizado` escolhe qual versao do saque sera usada,
 * para comparar o antes e o depois.
 */
public class SaqueTitular implements Runnable {

    private final ContaConjunta conta;
    private final String titular;
    private final double valor;
    private final boolean sincronizado;

    // resultado lido pelo main depois do join()
    private boolean aprovado;

    public SaqueTitular(ContaConjunta conta, String titular, double valor, boolean sincronizado) {
        this.conta = conta;
        this.titular = titular;
        this.valor = valor;
        this.sincronizado = sincronizado;
    }

    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();

        if (!conta.isTitular(titular)) {
            System.out.printf("[%s] %s não é titular da conta %d%n",
                    nomeThread, titular, conta.getNumero());
            return;
        }

        System.out.printf("[%s] %s solicita saque de R$ %.2f%n", nomeThread, titular, valor);

        aprovado = sincronizado
                ? conta.sacarSincronizado(valor)
                : conta.sacarSemSincronizacao(valor);

        System.out.printf("[%s] %s -> saque %s%n", nomeThread, titular,
                aprovado ? "REALIZADO" : "NEGADO (saldo insuficiente)");
    }

    public boolean isAprovado() {
        return aprovado;
    }
}
