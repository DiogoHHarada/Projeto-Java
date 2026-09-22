package service;

import exception.SaldoInsuficienteException;
import model.Conta;
import strategy.TarifaStrategy;

/**
 * "Context" do padrão Strategy (Aula 04 - seção 12).
 *
 * Recebe a estratégia como parâmetro e delega o cálculo para ela.
 * A estratégia pode ser trocada em tempo de execução sem alterar esta classe.
 */
public class TarifaService {

    // calcula a tarifa de acordo com a estratégia selecionada (não altera a conta)
    public double calcularTarifa(Conta conta, TarifaStrategy estrategia) {
        return estrategia.calcularTarifa(conta);
    }

    // calcula a tarifa e desconta do saldo da conta; devolve o valor descontado
    public double aplicarTarifa(Conta conta, TarifaStrategy estrategia)
            throws SaldoInsuficienteException {
        double tarifa = calcularTarifa(conta, estrategia);
        if (tarifa > 0) {
            conta.sacar(tarifa); // lança SaldoInsuficienteException se não houver saldo
        }
        return tarifa;
    }
}
