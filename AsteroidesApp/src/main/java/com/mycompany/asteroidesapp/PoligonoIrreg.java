package com.mycompany.asteroidesapp;

import java.util.List;

public class PoligonoIrreg {

    public List<Coordenada> vertices;

    public PoligonoIrreg(List<Coordenada> vertices) {
        this.vertices = vertices;
    }

    public List<Coordenada> getVertices() {
        return vertices;
    }

    public void setVertices(List<Coordenada> vertices) {
        this.vertices = vertices;
    }

}
