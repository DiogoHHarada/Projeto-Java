package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.SaldoInsuficienteException;
import model.ContaCorrente;
import rede.GerenciadorContas;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aula 11 - Etapa 2: ControladorContas (o "ControladorFilmes" do slide).
 *
 * Recebe TODAS as requisicoes que comecam com /contas, descobre qual endpoint
 * foi chamado (caminho + metodo HTTP), le/valida o JSON e chama o
 * GerenciadorContas, que executa as operacoes. Depois devolve JSON + status.
 *
 *   Metodo  Endpoint                      Sucesso
 *   GET     /contas                       200 lista
 *   GET     /contas/{numero}              200 conta
 *   POST    /contas                       201 conta criada
 *   PUT     /contas/{numero}              200 conta atualizada
 *   DELETE  /contas/{numero}              200 mensagem
 *   POST    /contas/{numero}/depositos    200 conta com novo saldo
 *   POST    /contas/{numero}/saques       200 conta com novo saldo
 *
 *   Erros: 400 dados invalidos / saldo insuficiente, 404 nao encontrado,
 *          405 metodo nao permitido, 409 conta ja existe, 500 erro interno.
 */
public class ControladorContas implements HttpHandler {

    private final GerenciadorContas gerenciador;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ControladorContas(GerenciadorContas gerenciador) {
        this.gerenciador = gerenciador;
    }

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        // "/contas/1001/saques" -> ["contas", "1001", "saques"]
        String[] partes = Arrays.stream(troca.getRequestURI().getPath().split("/"))
                .filter(p -> !p.isEmpty())
                .toArray(String[]::new);

        System.out.println(metodo + " " + troca.getRequestURI());

        try {
            if (partes.length == 1) {
                tratarColecao(troca, metodo);                                  // /contas
            } else if (partes.length == 2) {
                tratarConta(troca, metodo, lerNumero(partes[1]));              // /contas/{n}
            } else if (partes.length == 3) {
                tratarOperacao(troca, metodo, lerNumero(partes[1]), partes[2]); // /contas/{n}/...
            } else {
                enviarErro(troca, 404, "Endpoint não encontrado.");
            }

        } catch (SaldoInsuficienteException e) {
            // regra de negocio preservada: nao saca mais do que o saldo
            enviarErro(troca, 400, e.getMessage());
        } catch (JsonParseException e) {
            enviarErro(troca, 400, "JSON inválido no corpo da requisição.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // codigos lancados pelo GerenciadorContas (CONTA_INEXISTENTE etc.)
            tratarErroDeNegocio(troca, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(troca, 500, "Erro interno do servidor.");
        }
    }

    // ---------------------------------------------------------------
    // /contas
    // ---------------------------------------------------------------
    private void tratarColecao(HttpExchange troca, String metodo) throws IOException {
        switch (metodo) {
            case "GET":
                enviarJson(troca, 200, gerenciador.listar());
                break;

            case "POST": {
                ContaRequest dados = lerCorpo(troca, ContaRequest.class);
                if (dados.getNumero() == null || dados.getTitular() == null) {
                    enviarErro(troca, 400, "Informe \"numero\" e \"titular\" (\"saldo\" é opcional).");
                    return;
                }
                double saldo = dados.getSaldo() == null ? 0.0 : dados.getSaldo();
                ContaCorrente criada = gerenciador.cadastrar(dados.getNumero(), dados.getTitular(), saldo);

                troca.getResponseHeaders().set("Location", "/contas/" + criada.getNumero());
                enviarJson(troca, 201, criada);
                break;
            }

            default:
                metodoNaoPermitido(troca, "GET, POST");
        }
    }

    // ---------------------------------------------------------------
    // /contas/{numero}
    // ---------------------------------------------------------------
    private void tratarConta(HttpExchange troca, String metodo, int numero) throws IOException {
        switch (metodo) {
            case "GET":
                enviarJson(troca, 200, gerenciador.buscar(numero));
                break;

            case "PUT": {
                ContaRequest dados = lerCorpo(troca, ContaRequest.class);
                if (dados.getNumero() != null && dados.getNumero() != numero) {
                    enviarErro(troca, 400, "O número da conta não pode ser alterado.");
                    return;
                }
                if (dados.getSaldo() != null) {
                    enviarErro(troca, 400, "O saldo só muda por depósito ou saque.");
                    return;
                }
                enviarJson(troca, 200, gerenciador.atualizarTitular(numero, dados.getTitular()));
                break;
            }

            case "DELETE":
                gerenciador.remover(numero);
                enviarJson(troca, 200, Map.of("mensagem", "Conta " + numero + " removida."));
                break;

            default:
                metodoNaoPermitido(troca, "GET, PUT, DELETE");
        }
    }

    // ---------------------------------------------------------------
    // /contas/{numero}/depositos   e   /contas/{numero}/saques
    // ---------------------------------------------------------------
    private void tratarOperacao(HttpExchange troca, String metodo, int numero, String operacao)
            throws IOException, SaldoInsuficienteException {

        if (!operacao.equals("depositos") && !operacao.equals("saques")) {
            enviarErro(troca, 404, "Endpoint não encontrado.");
            return;
        }
        if (!metodo.equals("POST")) {
            metodoNaoPermitido(troca, "POST");
            return;
        }

        OperacaoRequest dados = lerCorpo(troca, OperacaoRequest.class);
        if (dados.getValor() == null) {
            enviarErro(troca, 400, "Informe o \"valor\" da operação.");
            return;
        }

        boolean deposito = operacao.equals("depositos");
        if (deposito) {
            gerenciador.depositar(numero, dados.getValor());
        } else {
            gerenciador.sacar(numero, dados.getValor());   // pode lancar SaldoInsuficienteException
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", deposito ? "Depósito realizado." : "Saque realizado.");
        resposta.put("valor", dados.getValor());
        resposta.put("conta", gerenciador.buscar(numero));
        enviarJson(troca, 200, resposta);
    }

    // ---------------------------------------------------------------
    // auxiliares: leitura do corpo, respostas e erros
    // ---------------------------------------------------------------

    /** Le o corpo da requisicao (texto) e converte o JSON em objeto Java com Gson. */
    private <T> T lerCorpo(HttpExchange troca, Class<T> tipo) throws IOException {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        T objeto = gson.fromJson(corpo, tipo);
        if (objeto == null) {
            throw new JsonParseException("corpo vazio");
        }
        return objeto;
    }

    private int lerNumero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NUMERO_INVALIDO");
        }
    }

    private void tratarErroDeNegocio(HttpExchange troca, String codigo) throws IOException {
        switch (codigo) {
            case "CONTA_INEXISTENTE":
                enviarErro(troca, 404, "Conta não encontrada.");
                break;
            case "CONTA_JA_EXISTE":
                enviarErro(troca, 409, "Já existe uma conta com esse número.");
                break;
            case "VALOR_INVALIDO":
                enviarErro(troca, 400, "Valor inválido: informe um número positivo.");
                break;
            case "NUMERO_INVALIDO":
                enviarErro(troca, 400, "Número de conta inválido.");
                break;
            case "TITULAR_INVALIDO":
                enviarErro(troca, 400, "O titular é obrigatório.");
                break;
            default:
                enviarErro(troca, 400, codigo);
        }
    }

    private void metodoNaoPermitido(HttpExchange troca, String permitidos) throws IOException {
        troca.getResponseHeaders().set("Allow", permitidos);
        enviarErro(troca, 405, "Método não permitido neste endpoint. Use: " + permitidos + ".");
    }

    private void enviarErro(HttpExchange troca, int status, String mensagem) throws IOException {
        enviarJson(troca, status, Map.of("erro", mensagem));
    }

    /** Converte o objeto em JSON e envia com o status informado. */
    private void enviarJson(HttpExchange troca, int status, Object corpo) throws IOException {
        byte[] resposta = gson.toJson(corpo).getBytes(StandardCharsets.UTF_8);

        troca.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        troca.sendResponseHeaders(status, resposta.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(resposta);
        }
        troca.close();
    }
}
