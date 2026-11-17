package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Aula 11 - Etapa 1: consumo de uma API EXTERNA de cotacao de moedas.
 *
 * API usada: AwesomeAPI (gratuita, sem cadastro/chave)
 *   GET https://economia.awesomeapi.com.br/json/last/USD-BRL
 *   200 -> {"USDBRL": { "code":"USD", "bid":"5.78", ... }}
 *   404 -> {"status":404,"code":"CoinNotExists","message":"moeda nao encontrada XYZ-BRL"}
 *
 * Fluxo (slide "Consumindo APIs REST com Java"):
 *   1. prepara a requisicao  2. envia  3. recebe a resposta
 *   4. verifica o status     5. processa o JSON
 */
public class CotacaoService {

    private static final String URL_BASE = "https://economia.awesomeapi.com.br/json/last/";

    // HttpClient pode (e deve) ser reaproveitado entre as consultas
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Gson gson = new Gson();

    /**
     * Consulta a cotacao atual de `moeda` em reais (ex.: "USD", "EUR", "BTC").
     * Nada fica armazenado: cada chamada busca o valor do momento.
     */
    public Cotacao consultar(String moeda) throws CotacaoException {
        String codigo = moeda == null ? "" : moeda.trim().toUpperCase();
        if (!codigo.matches("[A-Z]{3}")) {
            throw new CotacaoException("Código de moeda inválido: use 3 letras (ex.: USD, EUR).");
        }

        // 1) prepara a requisicao GET
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE + codigo + "-BRL"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        // 2) e 3) envia e aguarda a resposta
        HttpResponse<String> resposta;
        try {
            resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new CotacaoException("Tempo limite esgotado ao consultar a cotação.", e);
        } catch (IOException e) {
            throw new CotacaoException("Falha de comunicação com a API (verifique a internet): "
                    + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CotacaoException("Consulta interrompida.", e);
        }

        // 4) verifica o codigo HTTP
        int status = resposta.statusCode();
        if (status == 404) {
            throw new CotacaoException("Moeda não encontrada: " + codigo);
        }
        if (status != 200) {
            throw new CotacaoException("A API respondeu com erro HTTP " + status + ".");
        }

        // 5) interpreta o JSON: {"USDBRL": {...}} -> pega o objeto interno e converte com Gson
        try {
            JsonObject raiz = JsonParser.parseString(resposta.body()).getAsJsonObject();
            JsonObject dados = raiz.getAsJsonObject(codigo + "BRL");
            if (dados == null) {
                throw new CotacaoException("Resposta inválida: a cotação de " + codigo + " não veio no JSON.");
            }
            return gson.fromJson(dados, Cotacao.class);

        } catch (JsonParseException | IllegalStateException | ClassCastException e) {
            // corpo vazio, HTML, JSON com outro formato...
            throw new CotacaoException("Resposta inválida da API (JSON inesperado).", e);
        }
    }
}
