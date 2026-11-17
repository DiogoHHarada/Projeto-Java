package rede;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Aula 10 - Cliente do Gerenciador de Contas Bancarias em rede (TCP).
 *
 * Apenas apresenta o menu, MONTA a solicitacao no formato do protocolo,
 * envia ao servidor e exibe a resposta. Nenhuma regra de negocio fica aqui
 * (quem decide se ha saldo, se a conta existe etc. e o servidor).
 *
 * Execute o ServidorBanco ANTES deste arquivo.
 */
public class ClienteBanco {

    public static void main(String[] args) {
        String endereco = "localhost";             // outro PC: troque pelo IP do servidor
        int porta = ServidorBanco.PORTA;           // 5000

        try (Socket socket = new Socket(endereco, porta);
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter saida = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            System.out.println("Conectado ao servidor " + endereco + ":" + porta);

            while (true) {
                System.out.println();
                System.out.println("===== BANCO JAVA =====");
                System.out.println("1 - Consultar saldo");
                System.out.println("2 - Depositar");
                System.out.println("3 - Sacar");
                System.out.println("4 - Sair");
                System.out.print("Escolha uma opção: ");

                String opcao = teclado.readLine();
                if (opcao == null || opcao.trim().equals("4")) {
                    System.out.println("Encerrando o cliente.");
                    break;
                }

                String solicitacao = montarSolicitacao(opcao.trim(), teclado);
                if (solicitacao == null) {
                    System.out.println("Opção inválida.");
                    continue;
                }

                // protocolo: envia UMA linha e espera UMA linha de resposta
                saida.println(solicitacao);
                String resposta = entrada.readLine();

                if (resposta == null) {
                    System.out.println("O servidor encerrou a conexão.");
                    break;
                }

                System.out.println("Solicitação enviada: " + solicitacao);
                System.out.println("Resposta recebida:   " + resposta);
                System.out.println(">> " + traduzir(resposta));
            }

        } catch (IOException e) {
            // ex.: Connection refused -> o servidor nao esta rodando
            System.out.println("Erro de comunicação: " + e.getMessage());
        }
    }

    /** Monta a linha do protocolo a partir da opcao do menu (sem validar regras). */
    private static String montarSolicitacao(String opcao, BufferedReader teclado) throws IOException {
        switch (opcao) {
            case "1":
                return "CONSULTAR;" + perguntar(teclado, "Número da conta: ");
            case "2":
                return "DEPOSITAR;" + perguntar(teclado, "Número da conta: ")
                        + ";" + perguntar(teclado, "Valor do depósito: ");
            case "3":
                return "SACAR;" + perguntar(teclado, "Número da conta: ")
                        + ";" + perguntar(teclado, "Valor do saque: ");
            default:
                return null;
        }
    }

    private static String perguntar(BufferedReader teclado, String texto) throws IOException {
        System.out.print(texto);
        String lido = teclado.readLine();
        // ';' separa os campos do protocolo, entao nao pode vir dentro de um campo
        return lido == null ? "" : lido.trim().replace(";", "");
    }

    /** Converte a resposta do protocolo em uma frase para o usuario. */
    private static String traduzir(String resposta) {
        String[] partes = resposta.split(";");

        switch (partes[0]) {
            case "SALDO":
                return "Saldo atual: R$ " + partes[1];
            case "OK":
                String operacao = partes[1].equals("DEPOSITO_REALIZADO")
                        ? "Depósito realizado" : "Saque realizado";
                return partes.length > 2 ? operacao + ". Novo saldo: R$ " + partes[2] : operacao + ".";
            case "ERRO":
                switch (partes[1]) {
                    case "SALDO_INSUFICIENTE": return "Erro: saldo insuficiente.";
                    case "CONTA_INEXISTENTE":  return "Erro: conta não encontrada.";
                    case "VALOR_INVALIDO":     return "Erro: valor inválido.";
                    default:                   return "Erro: solicitação inválida.";
                }
            default:
                return "Resposta desconhecida.";
        }
    }
}
