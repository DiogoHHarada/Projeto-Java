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

        try (Connection c = new ConexaoDAO().getConexao(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, carro.getMarca());
            ps.setString(2, carro.getPlaca());
            ps.setDouble(3, carro.getPreco());
            ps.setString(4, carro.getCor());
            ps.execute();

            JOptionPane.showMessageDialog(null, "Cadastro Concluido");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "CarroDAO" + e);
        }
    }

    public ArrayList<CarroDTO> selecionarCarro() {
        String sql = "SELECT * FROM tblCarro";
        ArrayList<CarroDTO> lista = new ArrayList<>(); // Declare and initialize the ArrayList here

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
            JOptionPane.showMessageDialog(null, "Excluído");
        } catch (SQLException e) {
            Logger.getLogger(CarroDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public ArrayList<CarroDTO> pesquisarCarros(String Pesquisar) throws SQLException {
        String sql = "SELECT * FROM tblCarro WHERE marca LIKE ? OR placa LIKE ? OR cor LIKE ?";

        try (Connection c = new ConexaoDAO().getConexao(); PreparedStatement ps = c.prepareStatement(sql);) {

            ps.setString(1, "%" + Pesquisar + "%");
            ps.setString(2, "%" + Pesquisar + "%");
            ps.setString(3, "%" + Pesquisar + "%");

            ResultSet rs = ps.executeQuery();
            ArrayList<CarroDTO> carros = new ArrayList<>();

            while (rs.next()) {
                CarroDTO carro = new CarroDTO();
                carro.setId(rs.getInt("id"));
                carro.setMarca(rs.getString("marca"));
                carro.setPlaca(rs.getString("placa"));
                carro.setPreco(rs.getDouble("preco"));
                carro.setCor(rs.getString("cor"));

                carros.add(carro);
            }

            return carros;
        }
    }
}
