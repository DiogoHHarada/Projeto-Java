package api;

import com.sun.net.httpserver.HttpServer;
import rede.GerenciadorContas;
import service.ContaService;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Aula 11 - Etapa 2: API HTTP do Gerenciador de Contas Bancarias.
 *
 * Organizacao (igual ao slide "Responsabilidades da aplicacao"):
 *
 *   ServidorHTTP        -> recebe as requisicoes (HttpServer na porta 8080)
 *        |
 *   ControladorContas   -> interpreta os pedidos (caminho, metodo, JSON)
 *        |
 *   GerenciadorContas   -> executa as operacoes (contas em memoria)
 *        |
 *   ContaCorrente       -> representa os dados de cada conta
 *
 * Teste pelo navegador (GETs) ou pelo Postman/Insomnia (todos os metodos).
 */
public class ServidorHTTP {

    public static final int PORTA = 8080;

    public static void main(String[] args) throws IOException {
        // dados em uma colecao em memoria, iniciando com as contas do contas.txt
        GerenciadorContas gerenciador =
                new GerenciadorContas(new ContaService().carregarContas("contas.txt"));

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PORTA), 0);

        // createContext("/contas") atende /contas e tudo abaixo dele (/contas/1001/saques...)
        servidor.createContext("/contas", new ControladorContas(gerenciador));

        servidor.start();

        System.out.println("API do banco iniciada em http://localhost:" + PORTA);
        System.out.println("  GET    /contas");
        System.out.println("  GET    /contas/{numero}");
        System.out.println("  POST   /contas                       {\"numero\":1007,\"titular\":\"...\",\"saldo\":100.0}");
        System.out.println("  PUT    /contas/{numero}              {\"titular\":\"...\"}");
        System.out.println("  DELETE /contas/{numero}");
        System.out.println("  POST   /contas/{numero}/depositos    {\"valor\":200.0}");
        System.out.println("  POST   /contas/{numero}/saques       {\"valor\":50.0}");
    }
}
