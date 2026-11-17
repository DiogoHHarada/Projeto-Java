package concorrencia;

import exception.SaldoInsuficienteException;
import model.Conta;

/**
 * Aula 08 - Threads: representa UM atendimento bancario.
 *
 * Implementa Runnable: esta classe so DEFINE o trabalho (o que fazer).
 * Quem EXECUTA o trabalho em um novo fluxo e a Thread criada no main:
 *
 *     Thread t = new Thread(new AtendimentoBancario(...), "Atendimento-1");
 *     t.start();   // cria a nova thread, que chama run()
 */
public class AtendimentoBancario implements Runnable {

    /** Operacoes ja existentes no gerenciador que um atendimento pode fazer. */
    public enum Operacao {
        CONSULTA_SALDO("Consulta de saldo"),
        DEPOSITO("Depósito"),
        SAQUE("Saque"),
        DADOS_CONTA("Consulta dos dados da conta");

        private final String descricao;

        Operacao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private final Conta conta;
    private final Operacao operacao;
    private final double valor;            // usado so em DEPOSITO e SAQUE
    private final long tempoAtendimento;   // em milissegundos (Thread.sleep)

    /** Para operacoes que movimentam dinheiro (deposito e saque). */
    public AtendimentoBancario(Conta conta, Operacao operacao, double valor, long tempoAtendimento) {
        this.conta = conta;
        this.operacao = operacao;
        this.valor = valor;
        this.tempoAtendimento = tempoAtendimento;
    }

    /** Para operacoes de consulta (nao precisam de valor). */
    public AtendimentoBancario(Conta conta, Operacao operacao, long tempoAtendimento) {
        this(conta, operacao, 0, tempoAtendimento);
    }

    /** Codigo executado pela NOVA thread (chamado por start(), nunca diretamente). */
    @Override
    public void run() {
        String nomeThread = Thread.currentThread().getName();

        System.out.printf("[%s] INÍCIO  do atendimento | conta %d | %s%n",
                nomeThread, conta.getNumero(), operacao.getDescricao());

        try {
            // simula o tempo do atendimento e deixa a concorrencia visivel
            Thread.sleep(tempoAtendimento);

            String resultado = executarOperacao();

            System.out.printf("[%s] TÉRMINO do atendimento | conta %d | %s | %s%n",
                    nomeThread, conta.getNumero(), operacao.getDescricao(), resultado);

        } catch (InterruptedException e) {
            // boa pratica: restaura o "aviso" de interrupcao da thread
            Thread.currentThread().interrupt();
            System.out.printf("[%s] atendimento da conta %d INTERROMPIDO%n",
                    nomeThread, conta.getNumero());
        }
    }

    /** Reaproveita os metodos que a conta ja possuia (depositar, sacar, getSaldo...). */
    private String executarOperacao() {
        switch (operacao) {
            case CONSULTA_SALDO:
                return String.format("saldo atual: R$ %.2f", conta.getSaldo());

            case DEPOSITO:
                conta.depositar(valor);
                return String.format("depositado R$ %.2f -> novo saldo: R$ %.2f",
                        valor, conta.getSaldo());

            case SAQUE:
                try {
                    conta.sacar(valor);
                    return String.format("sacado R$ %.2f -> novo saldo: R$ %.2f",
                            valor, conta.getSaldo());
                } catch (SaldoInsuficienteException e) {
                    return "saque NÃO realizado: " + e.getMessage();
                }

            case DADOS_CONTA:
                return String.format("titular: %s | saldo: R$ %.2f",
                        conta.getTitular(), conta.getSaldo());

            default:
                return "operação desconhecida";
        }
    }
}
