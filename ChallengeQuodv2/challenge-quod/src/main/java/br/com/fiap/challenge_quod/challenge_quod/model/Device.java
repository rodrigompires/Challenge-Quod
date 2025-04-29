package br.com.fiap.challenge_quod.challenge_quod.model;

public class Device {
    private String fabricante;
    private String modelo;
    private String sistemaOperacional;

    public Device() {}

    public Device(String fabricante, String modelo, String sistemaOperacional) {
        this.fabricante = fabricante;
        this.modelo = modelo;
        this.sistemaOperacional = sistemaOperacional;
    }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getSistemaOperacional() { return sistemaOperacional; }
    public void setSistemaOperacional(String sistemaOperacional) { this.sistemaOperacional = sistemaOperacional; }

    @Override
    public String toString() {
        return "Dispositivo{" +
                "fabricante='" + fabricante + '\'' +
                ", modelo='" + modelo + '\'' +
                ", sistemaOperacional='" + sistemaOperacional + '\'' +
                '}';
    }
}