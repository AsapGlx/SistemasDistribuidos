package com.mycompany.asteroidesapp;

public class Asteroide extends PoligonoIrreg {

    private int numeroLados;

    public Asteroide(int numeroLados) {
        super(AsteroidesApp.generarVertices(numeroLados));
        this.numeroLados = numeroLados;
    }

    public double obtienePerimetro() {
        double perimetro = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            perimetro += vertices.get(i).distancia(vertices.get(i + 1));
        }
        return perimetro + vertices.get(vertices.size() - 1).distancia(vertices.get(0));
    }

}
