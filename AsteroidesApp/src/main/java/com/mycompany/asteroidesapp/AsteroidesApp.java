package com.mycompany.asteroidesapp;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random; 

public class AsteroidesApp extends JFrame {

    private int nAsteroides;
    private List<Asteroide> asteroides;
    private int ancho;
    private int alto;

    public AsteroidesApp(int nAsteroides) {
        this.nAsteroides = nAsteroides;
        this.asteroides = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < nAsteroides; i++) {
            int lados = random.nextInt(6) + 3;
            asteroides.add(new Asteroide(lados));  
        }
        this.ancho = 800;
        this.alto = 600;
        initComponents();
    }
    

    private void initComponents() {
        setSize(ancho, alto);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new Panel());
        setVisible(true);
    }

    private class Panel extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Asteroide asteroide : asteroides) {
                g.setColor(Color.blue);
                int[] xpoints = new int[asteroide.getVertices().size()];
                int[] ypoints = new int[asteroide.getVertices().size()];
                for (int i = 0; i < asteroide.getVertices().size(); i++) {
                    xpoints[i] = (int) asteroide.getVertices().get(i).abcisa();
                    ypoints[i] = (int) asteroide.getVertices().get(i).ordenada();
                }
                g.drawPolygon(xpoints, ypoints, asteroide.getVertices().size());

                g.setColor(Color.red);
                //g.drawString("Perímetro: " + asteroide.obtienePerimetro(), xpoints[0], ypoints[0]);
            }
        }
    }
     
    public void reordenarYRedibujar() {
        try {
            Thread.sleep(3000);  // Espera 3 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        // Ordena los asteroides por perímetro
        Collections.sort(asteroides, (a1, a2) -> Double.compare(a1.obtienePerimetro(), a2.obtienePerimetro()));
    
        // Redibuja los asteroides en el centro, uno por uno
         for (Asteroide asteroide : asteroides) {
        double dx = ancho / 2.0 - getCentroideX(asteroide.getVertices());
        double dy = alto / 2.0 - getCentroideY(asteroide.getVertices());

        trasladar(asteroide.getVertices(), dx, dy);  // Traslada el asteroide al centro de la pantalla
    
            repaint();  // Redibuja la pantalla
    
            try {
                Thread.sleep(500);  // Espera medio segundo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static List<Coordenada> generarVertices(int numeroLados) {
        List<Coordenada> vertices = new ArrayList<>();
        double angulo = 2 * Math.PI / numeroLados;
        double radio = 50 + Math.random() * 50;
        double centroX = radio + Math.random() * (800 - 2 * radio); // Asegura que el asteroide esté completamente dentro del panel
        double centroY = radio + Math.random() * (600 - 2 * radio);

        for (int i = 0; i < numeroLados; i++) {
            double theta = i * angulo;

            // Añadir un pequeño desplazamiento aleatorio al radio para hacer el asteroide irregular
            double radioIrregular = radio * (0.8 + 0.4 * Math.random()); // Asume que el radio varía entre 80% y 120% del radio original

            double x = radioIrregular * Math.cos(theta) + centroX;
            double y = radioIrregular * Math.sin(theta) + centroY;

            vertices.add(new Coordenada(x, y));
        }
        return vertices;
    }
    
    public double getCentroideX(List<Coordenada> vertices) {
        double sumaX = 0;
        for (Coordenada vertice : vertices) {
            sumaX += vertice.abcisa();
        }
        return sumaX / vertices.size();
    }

    public double getCentroideY(List<Coordenada> vertices) {
        double sumaY = 0;
        for (Coordenada vertice : vertices) {
            sumaY += vertice.ordenada();
        }
        return sumaY / vertices.size();
    }

    public void trasladar(List<Coordenada> vertices, double dx, double dy) {
        for (Coordenada vertice : vertices) {
            double nuevoX = vertice.abcisa() + dx;
            double nuevoY = vertice.ordenada() + dy;
            vertice.setAbcisa(nuevoX);
            vertice.setOrdenada(nuevoY);
        }
    }

    public static void main(String[] args) {
        //int nAsteroides = Integer.parseInt(args[0]);
        int nAsteroides = 25;
        AsteroidesApp app = new AsteroidesApp(nAsteroides);
        app.setVisible(true);
        app.reordenarYRedibujar();
    }

}