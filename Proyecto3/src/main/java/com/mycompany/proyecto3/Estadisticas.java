package com.mycompany.proyecto3;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Estadisticas extends JFrame {

    private Map<String, Integer> nivelesEstudio = new HashMap<>();
    private int totalCiudadanos = 0;
    private int maxValor = 0;

    public Estadisticas() {
        setTitle("Estadísticas de Nivel de Estudios");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Timer timer = new Timer(3000, e -> {
            leerRegistros();
            repaint();
        });
        timer.start();
    }

    private void leerRegistros() {
        nivelesEstudio.clear();
        totalCiudadanos = 0;
        maxValor = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("Curps.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 3) {
                    String nivel = partes[2];
                    nivelesEstudio.put(nivel, nivelesEstudio.getOrDefault(nivel, 0) + 1);
                    totalCiudadanos++;
                    maxValor = Math.max(maxValor, nivelesEstudio.get(nivel));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int y = 100;
        int maxAnchoBarra = getWidth() - 250; // Ajustar según necesidad

        for (Map.Entry<String, Integer> entry : nivelesEstudio.entrySet()) {
            int anchoBarra = (int) ((double) entry.getValue() / maxValor * maxAnchoBarra);
            g.drawString(entry.getKey() + ": " + entry.getValue(), 50, y);
            g.fillRect(200, y - 10, anchoBarra, 20);
            y += 30;
        }

        g.drawString("Total de ciudadanos: " + totalCiudadanos, 50, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Estadisticas().setVisible(true);
        });
    }
}
