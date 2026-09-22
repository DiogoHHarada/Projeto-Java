package dao;

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
}
