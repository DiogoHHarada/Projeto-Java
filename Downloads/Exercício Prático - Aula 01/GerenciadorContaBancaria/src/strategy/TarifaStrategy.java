package strategy;

import model.Conta;

/**
 * Padrão Strategy usando enum (Aula 04 - seção 6 e 12).
 *
 * Cada constante é uma "ConcreteStrategy": implementa o método abstrato
 * calcularTarifa() com a sua própria regra. Quem usa o enum (o "Context",
 * ver TarifaService) não precisa de if/else ou switch.
 */
public enum TarifaStrategy {

    FIXA("R$ 10,00 fixos") {
        @Override
        public double calcularTarifa(Conta conta) {
            return 10.0;
        }
    },

    PERCENTUAL("1% do saldo") {
        @Override
        public double calcularTarifa(Conta conta) {
            return conta.getSaldo() * 0.01;
        }
    },

    ISENTA("sem tarifa") {
        @Override
        public double calcularTarifa(Conta conta) {
            return 0.0;
        }
    };

    private final String descricao;

    TarifaStrategy(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // método abstrato: toda constante do enum é obrigada a implementar
    public abstract double calcularTarifa(Conta conta);

    @Override
    public String toString() {
        return name() + " (" + descricao + ")";
    }
}
