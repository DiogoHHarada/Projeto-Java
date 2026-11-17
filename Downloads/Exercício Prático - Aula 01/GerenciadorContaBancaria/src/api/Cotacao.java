package api;

import com.google.gson.annotations.SerializedName;

/**
 * Aula 11 - Etapa 1: cotacao de uma moeda em reais.
 *
 * Objeto Java que recebe os dados do JSON da AwesomeAPI, por exemplo:
 *   {"code":"USD","codein":"BRL","name":"Dólar Americano/Real Brasileiro",
 *    "high":"5.8091","low":"5.7594","pctChange":"-0.025932",
 *    "bid":"5.7825","ask":"5.7833","create_date":"2026-11-17 17:04:17"}
 *
 * @SerializedName liga o nome da chave no JSON ao atributo em portugues.
 * O Gson converte sozinho os numeros que vem como texto ("5.78") para double.
 */
public class Cotacao {

    @SerializedName("code")
    private String moeda;

    @SerializedName("codein")
    private String moedaDestino;

    @SerializedName("name")
    private String nome;

    @SerializedName("bid")
    private double compra;

    @SerializedName("ask")
    private double venda;

    @SerializedName("high")
    private double maxima;

    @SerializedName("low")
    private double minima;

    @SerializedName("pctChange")
    private double variacaoPercentual;

    @SerializedName("create_date")
    private String dataHora;

    public String getMoeda() {
        return moeda;
    }

    public String getMoedaDestino() {
        return moedaDestino;
    }

    public String getNome() {
        return nome;
    }

    public double getCompra() {
        return compra;
    }

    public double getVenda() {
        return venda;
    }

    public double getMaxima() {
        return maxima;
    }

    public double getMinima() {
        return minima;
    }

    public double getVariacaoPercentual() {
        return variacaoPercentual;
    }

    public String getDataHora() {
        return dataHora;
    }

    /** Exibicao organizada da cotacao (requisito 5). */
    @Override
    public String toString() {
        return String.format(
                "%s%n"
                + "  1 %s = R$ %.4f (compra) | R$ %.4f (venda)%n"
                + "  Máxima: R$ %.4f | Mínima: R$ %.4f | Variação: %+.2f%%%n"
                + "  Atualizado em: %s",
                nome, moeda, compra, venda, maxima, minima, variacaoPercentual, dataHora);
    }
}
