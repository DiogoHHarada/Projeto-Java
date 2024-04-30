/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author diogo
 */
public class ConexaoDAO {
    static String stringconexao = "jdbc:postgresql://localhost:5432/DiogoHarada";
    static String usuario = "postgres";
    static String senha = "postgres";

    static PreparedStatement prepareStatement(String sql) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public Connection getConexao()
    {
  
        try
        {
            return DriverManager.getConnection(stringconexao, usuario, senha);
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
}
