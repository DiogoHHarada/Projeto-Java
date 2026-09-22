package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaria de conexao JDBC (Aula 05 - Tarefa 2).
 *
 * Centraliza os parametros de conexao em constantes, como pede a aula.
 * ATENCAO: ajuste USER e PASS para os dados do SEU MySQL Workbench.
 */
public class Conexao {

    // jdbc:mysql://servidor:porta/nome_do_banco
    private static final String URL = "jdbc:mysql://localhost:3306/banco_digital";
    private static final String USER = "root";
    private static final String PASS = "aluno"; // <<< TROQUE pela senha do seu MySQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
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
