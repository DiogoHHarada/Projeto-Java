package model;

import exception.SaldoInsuficienteException;

/**
 * Aula 09 - Controle de concorrencia: CONTA CONJUNTA.
 *
 * Dois titulares movimentam a MESMA conta, possivelmente ao mesmo tempo
 * (cada um em uma thread). O saldo e o ESTADO COMPARTILHADO.
 *
 * Para comparar, existem duas versoes do saque:
 *   - sacarSemSincronizacao(): verifica e atualiza o saldo SEM protecao
 *                              -> condicao de corrida (saldo pode ficar negativo)
 *   - sacarSincronizado():     mesma logica, mas com synchronized
 *                              -> verificacao + atualizacao viram uma operacao atomica
 */
public class ContaConjunta extends Conta {

    private final String segundoTitular;

    public ContaConjunta(int numero, String titular, String segundoTitular, double saldo) {
        super(numero, titular, saldo);
        this.segundoTitular = segundoTitular;
    }

    public String getSegundoTitular() {
        return segundoTitular;
    }

    /** Os dois titulares podem movimentar a conta. */
    public boolean isTitular(String nome) {
        return getTitular().equalsIgnoreCase(nome) || segundoTitular.equalsIgnoreCase(nome);
    }

    // ------------------------------------------------------------------
    // ANTES: sem sincronizacao (Tarefa 4) - NAO USE em codigo real
    // ------------------------------------------------------------------

    /**
     * Saque INSEGURO. Duas threads podem passar pelo if ao mesmo tempo,
     * ambas enxergando saldo suficiente, e as duas descontam o valor.
     */
    public boolean sacarSemSincronizacao(double valor) {
        if (valor > 0 && valor <= saldo) {      // 1) verifica
            simularProcessamento();             //    (a outra thread entra aqui)
            saldo -= valor;                     // 2) atualiza com base em uma leitura antiga
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // DEPOIS: com synchronized (Tarefa 5)
    // ------------------------------------------------------------------

    /**
     * Saque SEGURO. synchronized usa o monitor DESTA conta (this): enquanto
     * um titular esta aqui dentro, o outro espera na porta. Assim a
     * verificacao e a atualizacao acontecem juntas, sem intercalacao.
     *
     * Retorna true se sacou, false se nao havia saldo (igual ao slide).
     */
    public synchronized boolean sacarSincronizado(double valor) {
        if (valor > 0 && valor <= saldo) {
            simularProcessamento();             // mesmo atraso: agora nao faz mal
            saldo -= valor;
            return true;
        }
        return false;
    }

    // Os demais acessos ao saldo usam o MESMO monitor (this), senao a
    // protecao nao vale para eles.

    @Override
    public synchronized void sacar(double valor) throws SaldoInsuficienteException {
        if (!sacarSincronizado(valor)) {        // reentrante: a thread ja tem o monitor
            throw new SaldoInsuficienteException("Saldo insuficiente ou valor inválido para saque.");
        }
    }

    @Override
    public synchronized void depositar(double valor) {
        super.depositar(valor);
    }

    @Override
    public synchronized double getSaldo() {
        return saldo;
    }

    /**
     * Pequena pausa entre verificar e atualizar o saldo. Representa o tempo
     * de processamento de um sistema real e torna a condicao de corrida
     * visivel em toda execucao (sem ela o defeito apareceria so as vezes).
     */
    private void simularProcessamento() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return numero + " - " + titular + " e " + segundoTitular
                + " - R$ " + String.format("%.2f", saldo);
    }
}
