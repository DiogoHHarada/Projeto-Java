package app;

import api.Cotacao;
import api.CotacaoException;
import api.CotacaoService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Aula 11 - Etapa 1: consulta de cotacao de moedas pelo console.
 *
 * Digite o codigo da moeda (USD, EUR, GBP, BTC...) e veja a cotacao atual em
 * reais, buscada na hora em uma API externa. ENTER vazio encerra.
 */
public class MainCotacao {

    public static void main(String[] args) throws IOException {
        CotacaoService service = new CotacaoService();
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("===== COTAÇÃO DE MOEDAS =====");

        while (true) {
            System.out.print("\nMoeda (ex.: USD, EUR, BTC) ou ENTER para sair: ");
            String moeda = teclado.readLine();
            if (moeda == null || moeda.isBlank()) {
                System.out.println("Encerrando.");
                break;
            }

            try {
                Cotacao cotacao = service.consultar(moeda);
                System.out.println(cotacao);
            } catch (CotacaoException e) {
                System.out.println("Não foi possível consultar: " + e.getMessage());
            }
        }
    }
}
