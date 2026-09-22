package dao;

import model.Transferencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da tabela `transferencias` (historico).
 *
 * IMPORTANTE: o metodo inserir() recebe uma Connection JA ABERTA em vez de
 * abrir a propria. E isso que faz o registro do historico entrar na MESMA
 * transacao da transferencia: se o debito ou o credito falhar, o rollback
 * desfaz tambem o INSERT do historico.
 */
public class TransferenciaDAO {

    /** Grava o historico DENTRO da transacao de quem chamou. Nao fecha a conexao. */
    public void inserir(Connection conn, Transferencia t) throws SQLException {
        String sql = "INSERT INTO transferencias "
                + "(conta_origem, conta_destino, valor, tarifa, modalidade_tarifa, data_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, t.getContaOrigem());
            stmt.setInt(2, t.getContaDestino());
            stmt.setDouble(3, t.getValor());
            stmt.setDouble(4, t.getTarifa());
            stmt.setString(5, t.getModalidadeTarifa());
            stmt.setTimestamp(6, Timestamp.valueOf(t.getDataHora()));
            stmt.executeUpdate();

            // devolve o id gerado pelo AUTO_INCREMENT
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    t.setId(chaves.getInt(1));
                }
            }
        }
    }

    /** Extrato completo, da transferencia mais recente para a mais antiga. */
    public List<Transferencia> listar() throws SQLException {
        String sql = "SELECT id, conta_origem, conta_destino, valor, tarifa, "
                + "modalidade_tarifa, data_hora FROM transferencias "
                + "ORDER BY data_hora DESC, id DESC";

        List<Transferencia> lista = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    /** Extrato de UMA conta: transferencias enviadas e recebidas. */
    public List<Transferencia> listarPorConta(int numero) throws SQLException {
        String sql = "SELECT id, conta_origem, conta_destino, valor, tarifa, "
                + "modalidade_tarifa, data_hora FROM transferencias "
                + "WHERE conta_origem = ? OR conta_destino = ? "
                + "ORDER BY data_hora DESC, id DESC";

        List<Transferencia> lista = new ArrayList<>();

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            stmt.setInt(2, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        }
        return lista;
    }

    /** Quantas transferencias existem para uma conta (usado antes de excluir). */
    public int contarPorConta(int numero) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transferencias WHERE conta_origem = ? OR conta_destino = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            stmt.setInt(2, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Apaga o historico de uma conta. Necessario antes de excluir a conta,
     * por causa da chave estrangeira. Devolve quantas linhas foram apagadas.
     */
    public int removerPorConta(int numero) throws SQLException {
        String sql = "DELETE FROM transferencias WHERE conta_origem = ? OR conta_destino = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            stmt.setInt(2, numero);
            return stmt.executeUpdate();
        }
    }

    /** Evita repetir a leitura do ResultSet nos metodos de consulta. */
    private Transferencia montar(ResultSet rs) throws SQLException {
        return new Transferencia(
                rs.getInt("id"),
                rs.getInt("conta_origem"),
                rs.getInt("conta_destino"),
                rs.getDouble("valor"),
                rs.getDouble("tarifa"),
                rs.getString("modalidade_tarifa"),
                rs.getTimestamp("data_hora").toLocalDateTime());
    }
}
