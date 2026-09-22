package dao;

import exception.SaldoInsuficienteException;
import model.Conta;
import model.ContaCorrente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) da tabela `contas` - Aula 05, Tarefa 3.
 *
 * Todo o SQL fica concentrado aqui; o resto do programa nao conhece o banco.
 * Todos os metodos usam PreparedStatement (evita SQL Injection) e
 * try-with-resources (fecha Connection/Statement/ResultSet automaticamente).
 */
public class ContaDAO {

    public void inserir(Conta conta) throws SQLException {
        String sql = "INSERT INTO contas (numero, titular, saldo) VALUES (?, ?, ?)";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, conta.getNumero());
            stmt.setString(2, conta.getTitular());
            stmt.setDouble(3, conta.getSaldo());
            stmt.executeUpdate();
        }
    }

    public List<ContaCorrente> listar() throws SQLException {
        String sql = "SELECT numero, titular, saldo FROM contas ORDER BY numero";
        List<ContaCorrente> contas = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                contas.add(new ContaCorrente(
                        rs.getInt("numero"),
                        rs.getString("titular"),
                        rs.getDouble("saldo")));
            }
        }
        return contas;
    }

    public ContaCorrente buscarPorNumero(int numero) throws SQLException {
        String sql = "SELECT numero, titular, saldo FROM contas WHERE numero = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ContaCorrente(
                            rs.getInt("numero"),
                            rs.getString("titular"),
                            rs.getDouble("saldo"));
                }
            }
        }
        return null; // nao encontrou
    }

    public boolean atualizarSaldo(int numero, double novoSaldo) throws SQLException {
        String sql = "UPDATE contas SET saldo = ? WHERE numero = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, novoSaldo);
            stmt.setInt(2, numero);
            return stmt.executeUpdate() > 0; // linhas afetadas
        }
    }

    public boolean remover(int numero) throws SQLException {
        String sql = "DELETE FROM contas WHERE numero = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            return stmt.executeUpdate() > 0;
        }
    }

    // ---------- Aula 06 - Tarefa 4: transferencia com TRANSACAO ----------

    /**
     * Transfere `valor` da conta de origem para a de destino.
     *
     * As duas operacoes (debito e credito) formam UMA unidade atomica:
     * ou as duas gravam (commit) ou nenhuma grava (rollback).
     *
     * O debito so acontece se `saldo >= valor` (garantido pelo proprio WHERE,
     * o que tambem evita saldo negativo se duas transferencias rodarem juntas).
     */
    public void transferir(int numeroOrigem, int numeroDestino, double valor)
            throws SQLException, SaldoInsuficienteException {

        if (numeroOrigem == numeroDestino) {
            throw new IllegalArgumentException("A conta de origem e a de destino são a mesma.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser positivo.");
        }

        // o WHERE ... AND saldo >= ? impede que o saldo fique negativo
        String sqlDebito = "UPDATE contas SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?";
        String sqlCredito = "UPDATE contas SET saldo = saldo + ? WHERE numero = ?";

        Connection conn = null;
        try {
            conn = Conexao.getConnection();
            conn.setAutoCommit(false); // ---- INICIA A TRANSACAO ----

            // 1) DEBITO na origem
            try (PreparedStatement stmt = conn.prepareStatement(sqlDebito)) {
                stmt.setDouble(1, valor);
                stmt.setInt(2, numeroOrigem);
                stmt.setDouble(3, valor);

                if (stmt.executeUpdate() == 0) {
                    // nenhuma linha afetada: ou a conta nao existe, ou faltou saldo
                    if (buscarPorNumero(conn, numeroOrigem) == null) {
                        throw new SQLException("Conta de origem " + numeroOrigem + " não existe.");
                    }
                    throw new SaldoInsuficienteException(
                            "Saldo insuficiente na conta " + numeroOrigem + " para transferir R$ "
                            + String.format("%.2f", valor) + ".");
                }
            }

            // 2) CREDITO no destino
            try (PreparedStatement stmt = conn.prepareStatement(sqlCredito)) {
                stmt.setDouble(1, valor);
                stmt.setInt(2, numeroDestino);

                if (stmt.executeUpdate() == 0) {
                    // destino inexistente: o debito acima precisa ser DESFEITO
                    throw new SQLException("Conta de destino " + numeroDestino + " não existe.");
                }
            }

            conn.commit(); // ---- CONFIRMA AS DUAS OPERACOES ----

        } catch (SQLException | SaldoInsuficienteException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ---- DESFAZ TUDO ----
                } catch (SQLException ex) {
                    System.err.println("Falha no rollback: " + ex.getMessage());
                }
            }
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // devolve a conexao ao estado normal
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Falha ao fechar conexão: " + ex.getMessage());
                }
            }
        }
    }

    // busca usando uma conexao JA ABERTA (para enxergar o que a transacao fez)
    private ContaCorrente buscarPorNumero(Connection conn, int numero) throws SQLException {
        String sql = "SELECT numero, titular, saldo FROM contas WHERE numero = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ContaCorrente(
                            rs.getInt("numero"),
                            rs.getString("titular"),
                            rs.getDouble("saldo"));
                }
            }
        }
        return null;
    }
}
