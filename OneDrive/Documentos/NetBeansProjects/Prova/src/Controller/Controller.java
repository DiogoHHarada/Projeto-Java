/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.CarroDAO;
import DTO.CarroDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author diogo
 */
public class Controller {
    
    public static void cadastrar(String marca, String placa, double preco, String cor) {
        if ( marca.isEmpty() || placa.isEmpty()||preco < 10000 || cor.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos.\nO campo preço deve ser de pelo menos 10000.");
            return;
        }

        CarroDTO objCarrodto = new CarroDTO();
        objCarrodto.setMarca(marca);
        objCarrodto.setPlaca(placa);
        objCarrodto.setPreco(preco);
        objCarrodto.setCor(cor);

        CarroDAO objCarrodao = new CarroDAO();
        objCarrodao.cadastrarCarro(objCarrodto);
    }
    public static void alterar(int id,String marca, String placa, double preco, String cor){
        if (marca.isEmpty() || placa.isEmpty() || preco < 10000 || cor.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos.\nO campo preço deve ser de pelo menos 10000.");
            return;
        }

        
        CarroDTO objCarrodto = new CarroDTO();
        objCarrodto.setId(id);
        objCarrodto.setMarca(marca);
        objCarrodto.setPlaca(placa);
        objCarrodto.setPreco(preco);
        objCarrodto.setCor(cor);

        CarroDAO objCarrodao = new CarroDAO();
        objCarrodao.UpdateCarro(objCarrodto);
    }
    
    public static void excluir(int id){
        CarroDAO objCarrodao = new CarroDAO();
        objCarrodao.DeleteCarro(id);
    }
    
    public static void limpar(JTextField Id,JTextField Marca, JTextField Placa,JTextField Preco, JTextField Cor){
        
        Id.setText(" ");
        Marca.setText(" ");
        Placa.setText(" ");
        Preco.setText(" ");
        Cor.setText(" ");
    }
    
    public static void carregar(JTextField Id,JTextField Marca, JTextField Placa,JTextField Preco, JTextField Cor, JTable tblCarro){
        int setar = tblCarro.getSelectedRow();

        Id.setText(tblCarro.getModel().getValueAt(setar, 0).toString());
        Marca.setText(tblCarro.getModel().getValueAt(setar, 1).toString());
        Placa.setText(tblCarro.getModel().getValueAt(setar, 2).toString());
        Preco.setText(tblCarro.getModel().getValueAt(setar, 3).toString());
        Cor.setText(tblCarro.getModel().getValueAt(setar, 4).toString());
    }
    
    public static void pesquisar(String Pesquisar, JTable tblCarro) {
        if (Pesquisar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, insira um termo de pesquisa.");
            return;
        }

        try {
            CarroDAO carroDAO = new CarroDAO();
            ArrayList<CarroDTO> carros = carroDAO.pesquisarCarros(Pesquisar);

            DefaultTableModel tableModel = (DefaultTableModel) tblCarro.getModel();
            tableModel.setRowCount(0);

            for (CarroDTO carro : carros) {
                Object[] row = {
                    carro.getId(),
                    carro.getMarca(),
                    carro.getPlaca(),
                    carro.getPreco(),
                    carro.getCor()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao pesquisar: " + e.getMessage() + "\n"
                    + "Por favor, verifique se o termo de pesquisa está correto.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void ListarValores(JTable tblCarro){
        try
        {
            CarroDAO objCarrodao = new CarroDAO();
            DefaultTableModel model = (DefaultTableModel) tblCarro.getModel();
            model.setNumRows(0);
            
            ArrayList<CarroDTO> lista = objCarrodao.selecionarCarro();
            
            for(int num = 0; num < lista.size(); num++)
            {
                model.addRow(new Object[]
                {
                    lista.get(num).getId(),
                    lista.get(num).getMarca(),
                    lista.get(num).getPlaca(),
                    lista.get(num).getPreco(),
                    lista.get(num).getCor(),

                });
            }
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Listar valores VIEW"+ e);
        }
    
    }
}

