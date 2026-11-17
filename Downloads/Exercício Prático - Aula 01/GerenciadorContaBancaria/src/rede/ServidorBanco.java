package rede;

import exception.SaldoInsuficienteException;
import service.ContaService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Aula 10 - Servidor do Gerenciador de Contas Bancarias em rede (TCP).
 *
 * Recebe e INTERPRETA as solicitacoes; quem executa e o GerenciadorContas.
 *
 * Protocolo de aplicacao (uma linha de texto por mensagem, campos separados por ';'):
 *
 *   Solicitacao                 Resposta de sucesso
 *   CONSULTAR;1001              SALDO;1500.00
 *   DEPOSITAR;1001;200.00       OK;DEPOSITO_REALIZADO;1700.00   (ultimo campo = novo saldo)
 *   SACAR;1001;50.00            OK;SAQUE_REALIZADO;1650.00
 *
 *   Erros: ERRO;SALDO_INSUFICIENTE | ERRO;CONTA_INEXISTENTE
 *          ERRO;VALOR_INVALIDO     | ERRO;COMANDO_INVALIDO
 *
 * Execute ESTE arquivo primeiro e depois o ClienteBanco.
 */
public class ServidorBanco {

    public static final int PORTA = 5000;

    private final GerenciadorContas gerenciador;

    public ServidorBanco(GerenciadorContas gerenciador) {
        this.gerenciador = gerenciador;
    }

    public static void main(String[] args) {
        GerenciadorContas gerenciador;
        try {
            // contas em memoria, carregadas do mesmo contas.txt das aulas anteriores
            gerenciador = new GerenciadorContas(new ContaService().carregarContas("contas.txt"));
        } catch (IOException e) {
            System.out.println("Erro ao carregar contas.txt: " + e.getMessage());
            return;
        }

        new ServidorBanco(gerenciador).iniciar();
    }

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor do banco na porta " + PORTA + " ("
                    + gerenciador.quantidadeContas() + " contas em memória).");

            // atende um cliente de cada vez, para sempre
            while (true) {
                System.out.println("Servidor aguardando conexão...");
                atenderCliente(servidor.accept());   // accept() BLOQUEIA ate alguem conectar
            }

        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    /** Conversa com UM cliente: le uma linha, responde uma linha, ate ele desconectar. */
    private void atenderCliente(Socket socket) {
        try (Socket cliente = socket;
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter saida = new PrintWriter(
                     new OutputStreamWriter(cliente.getOutputStream(), StandardCharsets.UTF_8),
                     true)) {   // true = autoFlush no println

            System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());

            String solicitacao;
            // readLine() devolve null quando o cliente fecha a conexao
            while ((solicitacao = entrada.readLine()) != null) {
                String resposta = processar(solicitacao);
                System.out.println("Recebido: " + solicitacao + "  ->  Resposta: " + resposta);
                saida.println(resposta);
            }

            System.out.println("Cliente desconectado.");

        } catch (IOException e) {
            System.out.println("Conexão encerrada com erro: " + e.getMessage());
        }
    }

    /** Interpreta UMA solicitacao do protocolo e devolve a resposta (uma linha). */
    public String processar(String solicitacao) {
        String[] partes = solicitacao.trim().split(";");
        String comando = partes[0].trim().toUpperCase();

        try {
            switch (comando) {
                case "CONSULTAR": {
                    exigirCampos(partes, 2);
                    double saldo = gerenciador.consultarSaldo(lerNumero(partes[1]));
                    return "SALDO;" + formatar(saldo);
                }
                case "DEPOSITAR": {
                    exigirCampos(partes, 3);
                    double novoSaldo = gerenciador.depositar(lerNumero(partes[1]), lerValor(partes[2]));
                    return "OK;DEPOSITO_REALIZADO;" + formatar(novoSaldo);
                }
                case "SACAR": {
                    exigirCampos(partes, 3);
                    double novoSaldo = gerenciador.sacar(lerNumero(partes[1]), lerValor(partes[2]));
                    return "OK;SAQUE_REALIZADO;" + formatar(novoSaldo);
                }
                default:
                    return "ERRO;COMANDO_INVALIDO";
            }

        } catch (SaldoInsuficienteException e) {
            return "ERRO;SALDO_INSUFICIENTE";
        } catch (IllegalArgumentException e) {
            // a mensagem ja e o codigo do erro (CONTA_INEXISTENTE, VALOR_INVALIDO...)
            return "ERRO;" + e.getMessage();
        }
    }

    // ---------- auxiliares de interpretacao ----------

    private void exigirCampos(String[] partes, int quantidade) {
        if (partes.length != quantidade) {
            throw new IllegalArgumentException("COMANDO_INVALIDO");
        }
    }

    private int lerNumero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CONTA_INEXISTENTE");
        }
    }

    private double lerValor(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VALOR_INVALIDO");
        }
    }

    /** Sempre com ponto decimal (1000.00), para o cliente conseguir ler. */
    private String formatar(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}
