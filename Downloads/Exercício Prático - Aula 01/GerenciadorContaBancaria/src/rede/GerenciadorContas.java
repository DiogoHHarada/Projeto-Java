package rede;

import exception.SaldoInsuficienteException;
import model.ContaCorrente;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aula 10 - GerenciadorContas: localiza as contas e executa as operacoes.
 *
 * Fica do lado do SERVIDOR e guarda as contas EM MEMORIA (um Map numero -> conta).
 * Todas as regras de negocio estao aqui (ou na propria ContaCorrente);
 * o cliente apenas pede as operacoes.
 *
 * A "ContaBancaria" sugerida no slide e a ContaCorrente que o projeto ja tinha.
 */
public class GerenciadorContas {

    private final Map<Integer, ContaCorrente> contas = new HashMap<>();

    public GerenciadorContas(List<ContaCorrente> contasIniciais) {
        for (ContaCorrente conta : contasIniciais) {
            contas.put(conta.getNumero(), conta);
        }
    }

    public int quantidadeContas() {
        return contas.size();
    }

    // synchronized: se no futuro varios clientes forem atendidos ao mesmo tempo
    // (uma thread por cliente), o saldo continua protegido (Aula 09).

    public synchronized double consultarSaldo(int numero) {
        return localizar(numero).getSaldo();
    }

    /** Deposita e devolve o novo saldo. */
    public synchronized double depositar(int numero, double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("VALOR_INVALIDO");
        }
        ContaCorrente conta = localizar(numero);
        conta.depositar(valor);
        return conta.getSaldo();
    }

    /** Saca e devolve o novo saldo. A regra do saldo e da propria ContaCorrente. */
    public synchronized double sacar(int numero, double valor) throws SaldoInsuficienteException {
        if (valor <= 0) {
            throw new IllegalArgumentException("VALOR_INVALIDO");
        }
        ContaCorrente conta = localizar(numero);
        conta.sacar(valor);   // lanca SaldoInsuficienteException se valor > saldo
        return conta.getSaldo();
    }

    private ContaCorrente localizar(int numero) {
        ContaCorrente conta = contas.get(numero);
        if (conta == null) {
            throw new IllegalArgumentException("CONTA_INEXISTENTE");
        }
        return conta;
    }
}
