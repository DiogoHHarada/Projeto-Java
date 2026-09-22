package model;

import java.time.LocalDateTime;

/**
 * Registro de uma transferencia realizada (linha da tabela `transferencias`).
 *
 * Guarda valor e tarifa SEPARADOS para permitir auditoria de quanto o banco
 * cobrou em cada operacao.
 */
public class Transferencia {

    private int id;
    private int contaOrigem;
    private int contaDestino;
    private double valor;
    private double tarifa;
    private String modalidadeTarifa;
    private LocalDateTime dataHora;

    public Transferencia(int id, int contaOrigem, int contaDestino, double valor,
            double tarifa, String modalidadeTarifa, LocalDateTime dataHora) {
        this.id = id;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.tarifa = tarifa;
        this.modalidadeTarifa = modalidadeTarifa;
        this.dataHora = dataHora;
    }

    public int getId() {
        return id;
    }

    public int getContaOrigem() {
        return contaOrigem;
    }

    public int getContaDestino() {
        return contaDestino;
    }

    public double getValor() {
        return valor;
    }

    public double getTarifa() {
        return tarifa;
    }

    public String getModalidadeTarifa() {
        return modalidadeTarifa;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setId(int id) {
        this.id = id;
    }

    /** Quanto saiu de fato da conta de origem (valor + tarifa). */
    public double getTotalDebitado() {
        return valor + tarifa;
    }

    @Override
    public String toString() {
        return String.format("#%d  %d -> %d  R$ %.2f (tarifa R$ %.2f, %s)",
                id, contaOrigem, contaDestino, valor, tarifa, modalidadeTarifa);
    }
}
