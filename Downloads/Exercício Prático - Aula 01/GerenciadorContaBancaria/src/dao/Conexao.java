package dao;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaria de conexao JDBC (Aula 05 - Tarefa 2 / Aula 06 - Tarefa 2).
 *
 * Nao guarda mais URL/usuario/senha: pega tudo de DatabaseConfig, que le do
 * ambiente. Veja config/DatabaseConfig.java para saber como configurar.
 */
public class Conexao {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD);
    }

    // Fechamento manual (com try-with-resources isso nao e necessario)
    public static void fechar(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexao: " + e.getMessage());
            }
        }
    }
}
