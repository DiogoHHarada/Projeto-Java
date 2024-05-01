/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import DAO.CarroDAO;
import DTO.CarroDTO;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author diogo
 */
public class Controller {
    
    public static void cadastrar(String marca, String placa, double preco, String cor) {
        if ( marca.isEmpty() || placa.isEmpty()||preco < 10000 || cor.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos.");
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
    
}
