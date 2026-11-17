package app;

import concorrencia.AtendimentoBancario;
import concorrencia.AtendimentoBancario.Operacao;
import model.ContaCorrente;
import service.ContaService;

import java.io.IOException;
import java.util.List;

/**
 * Aula 08 - Threads e Concorrencia (atividade pratica).
 *
 * Simula varios atendimentos bancarios acontecendo "ao mesmo tempo":
 * cada atendimento e um Runnable executado por uma Thread propria.
 *
 * Execute mais de uma vez (Shift+F6 neste arquivo) e compare a ordem das
 * mensagens: ela muda, porque quem decide qual thread roda e o ESCALONADOR,
 * nao a ordem dos start().
 */
public class MainAtendimentos {

    public static void main(String[] args) {
        ContaService service = new ContaService();

        // contas existentes no sistema (mesmo arquivo usado nas aulas anteriores)
        List<ContaCorrente> contas;
        try {
            contas = service.carregarContas("contas.txt");
        } catch (IOException e) {
            System.out.println("Erro ao carregar contas.txt: " + e.getMessage());
            return;
        }

        // 1) cria os atendimentos (Runnable = a tarefa)
        AtendimentoBancario a1 = new AtendimentoBancario(
                buscar(contas, 1001), Operacao.CONSULTA_SALDO, tempoAleatorio());
        AtendimentoBancario a2 = new AtendimentoBancario(
                buscar(contas, 1002), Operacao.DEPOSITO, 500.00, tempoAleatorio());
        AtendimentoBancario a3 = new AtendimentoBancario(
                buscar(contas, 1003), Operacao.SAQUE, 200.00, tempoAleatorio());
        AtendimentoBancario a4 = new AtendimentoBancario(
                buscar(contas, 1004), Operacao.DADOS_CONTA, tempoAleatorio());

        // 2) cada atendimento ganha a sua Thread (Thread = o fluxo de execucao)
        Thread t1 = new Thread(a1, "Atendimento-1");
        Thread t2 = new Thread(a2, "Atendimento-2");
        Thread t3 = new Thread(a3, "Atendimento-3");
        Thread t4 = new Thread(a4, "Atendimento-4");

        System.out.println("[" + Thread.currentThread().getName() + "] Abrindo os atendimentos...");

        // 3) start(): cria a nova thread e ela executa run() (NAO chamar run() direto)
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        // 4) join(): o main espera cada atendimento terminar antes de seguir
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("O programa principal foi interrompido enquanto aguardava.");
            return;
        }

        // 5) so chega aqui depois que TODAS as threads terminaram
        System.out.println("[" + Thread.currentThread().getName()
                + "] Todos os atendimentos foram concluídos.");
    }

    /** Localiza uma conta da lista pelo numero. */
    private static ContaCorrente buscar(List<ContaCorrente> contas, int numero) {
        return contas.stream()
                .filter(c -> c.getNumero() == numero)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Conta " + numero + " não encontrada."));
    }

    /** Tempo de atendimento entre 500 e 1500 ms, para a ordem variar entre execucoes. */
    private static long tempoAleatorio() {
        return 500 + (long) (Math.random() * 1000);
    }
}
