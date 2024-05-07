/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

/**
 *
 * @author diogo
 */
public class CarroDTO {
    private int id;
    private String Marca;
    private String Placa;
    private double Preco;
    private String Cor;

    public String getCor() {
        return Cor;
    }
    
    public void setCor(String Cor) 
    {
        this.Cor = Cor;
    }
    
    public int getId() {
        return id;
    }

    public String getMarca() {
        return Marca;
    }

    public String getPlaca() {
        return Placa;
    }

    public double getPreco() {
        return Preco;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMarca(String Marca) {
        this.Marca = Marca;
    }

    public void setPlaca(String Placa) {
        this.Placa = Placa;
    }

    public void setPreco(double Preco) {
        this.Preco = Preco;
    }

    

}
