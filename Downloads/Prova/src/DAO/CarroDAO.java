/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import DTO.CarroDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author diogo
 */
public class CarroDAO {
    Connection c;
    PreparedStatement ps;
    ResultSet rs;
    ArrayList<CarroDTO> lista = new ArrayList<>();

    public void cadastrarCarro(CarroDTO carro) {
        String sql = "INSERT INTO tblCarro(marca, placa, preco, cor) VALUES(?,?,?,?)";
        ps = null;
        c = new ConexaoDAO().getConexao();
        try {
            ps = c.prepareStatement(sql);
            ps.setString(1, carro.getMarca());
            ps.setString(2, carro.getPlaca());
            ps.setDouble(3, carro.getPreco());
            ps.setString(4, carro.getCor());
            ps.execute();
            JOptionPane.showMessageDialog(null, "Cadastro Concluido");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "CarroDAO" + e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(CarroDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ArrayList<CarroDTO> selecionarCarro() {
        String sql = "SELECT * FROM tblCarro";

        try (Connection c = new ConexaoDAO().getConexao(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                CarroDTO objCarrodto = new CarroDTO();
                objCarrodto.setId(rs.getInt("id"));
                objCarrodto.setMarca(rs.getString("marca"));
                objCarrodto.setPlaca(rs.getString("placa"));
                objCarrodto.setPreco(rs.getDouble("preco"));
                objCarrodto.setCor(rs.getString("cor"));


                lista.add(objCarrodto);
            }
        } catch (SQLException e) {
            Logger.getLogger(CarroDAO.class.getName()).log(Level.SEVERE, null, e);
        }

        return lista;
    }

    public void UpdateCarro(CarroDTO objCarroDTO) {
        String sql = "UPDATE tblCarro SET marca = ?, placa = ?, preco = ?, cor = ? where id = ?";
        try (Connection c = new ConexaoDAO().getConexao(); PreparedStatement ps = c.prepareStatement(sql);) {
            ps.setString(1, objCarroDTO.getMarca());
            ps.setString(2, objCarroDTO.getPlaca());
            ps.setDouble(3, objCarroDTO.getPreco()); 
            ps.setString(4, objCarroDTO.getCor());
            System.out.println(objCarroDTO.getId());
            ps.setInt(5, objCarroDTO.getId());
            ps.execute();
            ps.close();
            JOptionPane.showMessageDialog(null, "Edição Concluida");
        } catch (SQLException e) {
            Logger.getLogger(CarroDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void DeleteCarro(int id) {
        String sql = "DELETE FROM tblCarro WHERE id = ?";

        try (Connection c = new ConexaoDAO().getConexao(); PreparedStatement ps = c.prepareStatement(sql);) {
            ps.setInt(1, id);
            ps.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Excluído");
        }
    }
}
