package com.mycompany.asteroidesapp;

public class Coordenada {

    private double x, y;

    public Coordenada(double x, double y) {
        this.x = x;
        this.y = y;
    }

    //Metodo getter de x

    public double abcisa() { return x; }

    //Metodo getter de y

    public double ordenada() { return y; }

    //Sobreescritura del método de la superclase objeto para imprimir con System.out.println()

    public void setAbcisa(double x) { this.x = x; }

    public void setOrdenada(double y) { this.y = y; }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    public double distancia(Coordenada otra) {
        return Math.sqrt(Math.pow(otra.x - this.x, 2) + Math.pow(otra.y - this.y, 2));
    }
    

}
