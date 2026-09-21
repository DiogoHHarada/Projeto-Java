package service;

import exception.SaldoInsuficienteException;
import model.ContaCorrente;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContaService {

    // Faixas de saldo usadas no agrupamento (na ordem em que devem ser exibidas)
    public static final String FAIXA_ATE_5000 = "Até R$ 5000";
    public static final String FAIXA_5001_A_10000 = "De R$ 5001 a R$ 10000";
    public static final String FAIXA_ACIMA_10000 = "Acima de R$ 10000";
    public static final String[] FAIXAS = {FAIXA_ATE_5000, FAIXA_5001_A_10000, FAIXA_ACIMA_10000};

    public ContaCorrente lerConta(String caminho) throws IOException {
        List<String> linhas = Files.readAllLines(Paths.get(caminho));
        String[] dados = linhas.get(0).split(",");
        int numero = Integer.parseInt(dados[0].trim());
        String titular = dados[1].trim();
        double saldo = Double.parseDouble(dados[2].trim());
        return new ContaCorrente(numero, titular, saldo);
    }

    public void sacarValor(ContaCorrente conta, double valor) throws SaldoInsuficienteException {
        conta.sacar(valor);
    }

    public void atualizarConta(ContaCorrente conta, String caminho) throws IOException {
        String dados = conta.getNumero() + "," + conta.getTitular() + "," + conta.getSaldo();
        Files.write(Paths.get(caminho), dados.getBytes());
    }

    public List<ContaCorrente> carregarContas(String caminho) throws IOException {
        List<ContaCorrente> contas = new ArrayList<>();
        List<String> linhas = Files.readAllLines(Paths.get(caminho));

        for (String linha : linhas) {
            if (linha == null || linha.trim().isEmpty()) {
                continue; // pula linha vazia
            }
            String[] dados = linha.split(",");
            if (dados.length < 3) {
                continue; // pula linha mal formatada
            }
            try {
                int numero = Integer.parseInt(dados[0].trim());
                String titular = dados[1].trim();
                double saldo = Double.parseDouble(dados[2].trim());
                contas.add(new ContaCorrente(numero, titular, saldo));
            } catch (NumberFormatException e) {
                // linha inválida: ignora e continua
            }
        }
        return contas;
    }

    public void depositarValor(ContaCorrente conta, double valor) {
        conta.depositar(valor);
    }

    public void salvarContas(List<ContaCorrente> contas, String caminho) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (ContaCorrente conta : contas) {
            sb.append(conta.getNumero()).append(",")
                    .append(conta.getTitular()).append(",")
                    .append(conta.getSaldo())
                    .append(System.lineSeparator());
        }
        Files.write(Paths.get(caminho), sb.toString().getBytes());
    }

    // ---------- Funcionalidades com Streams API ----------

    // 15.1 - Filtra as contas com saldo superior a R$ 10.000
    public List<ContaCorrente> filtrarContasSaldoAlto(List<ContaCorrente> contas) {
        return contas.stream()
                .filter(conta -> conta.getSaldo() > 10000)
                .collect(Collectors.toList());
    }

    // 15.2 - Calcula o saldo total de todas as contas usando reduce()
    public double calcularSaldoTotal(List<ContaCorrente> contas) {
        return contas.stream()
                .map(ContaCorrente::getSaldo)
                .reduce(0.0, Double::sum);
    }

    // 15.3 - Agrupa as contas por faixa de saldo usando Collectors.groupingBy
    public Map<String, List<ContaCorrente>> agruparPorFaixaSaldo(List<ContaCorrente> contas) {
        return contas.stream()
                .collect(Collectors.groupingBy(conta -> {
                    if (conta.getSaldo() <= 5000) {
                        return FAIXA_ATE_5000;
                    } else if (conta.getSaldo() <= 10000) {
                        return FAIXA_5001_A_10000;
                    } else {
                        return FAIXA_ACIMA_10000;
                    }
                }));
    }

}
