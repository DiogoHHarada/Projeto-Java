package strategy;

/**
 * Padrao Strategy com enum: modalidades de tarifa da TRANSFERENCIA.
 *
 * Diferente de TarifaStrategy (que calcula sobre o saldo da conta), aqui a
 * tarifa e calculada sobre o VALOR transferido.
 *
 * Cada constante e uma ConcreteStrategy: implementa calcularTarifa() com a sua
 * propria regra, sem nenhum if/switch no resto do sistema. Para criar uma
 * modalidade nova basta acrescentar uma constante (principio Aberto/Fechado).
 */
public enum TarifaTransferenciaStrategy {

    ISENTA("Isenta - sem tarifa") {
        @Override
        public double calcularTarifa(double valorTransferencia) {
            return 0.0;
        }
    },

    FIXA("Fixa - R$ 5,00 por transferencia") {
        @Override
        public double calcularTarifa(double valorTransferencia) {
            return 5.0;
        }
    },

    PERCENTUAL("Percentual - 1% do valor") {
        @Override
        public double calcularTarifa(double valorTransferencia) {
            return valorTransferencia * 0.01;
        }
    },

    MISTA("Mista - R$ 2,00 + 0,5% do valor") {
        @Override
        public double calcularTarifa(double valorTransferencia) {
            return 2.0 + (valorTransferencia * 0.005);
        }
    };

    private final String descricao;

    TarifaTransferenciaStrategy(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Toda constante do enum e obrigada a implementar. */
    public abstract double calcularTarifa(double valorTransferencia);

    @Override
    public String toString() {
        return descricao; // aparece assim no JOptionPane da GUI
    }
}
