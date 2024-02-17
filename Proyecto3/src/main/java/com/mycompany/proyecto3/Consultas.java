package com.mycompany.proyecto3;

import java.io.*;
import java.util.*;

public class Consultas {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nMenú de Consultas:");
            System.out.println("1. Mensajes SMS por sexo.");
            System.out.println("2. Mensajes SMS por entidad federativa.");
            System.out.println("3. Ciudadanos por nivel de estudios y sexo.");
            System.out.println("4. Edad promedio por nivel de estudios.");
            System.out.println("5. Salir.");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();  // Consumir el salto de línea

            switch (opcion) {
                case 1:
                    contarMensajesPorSexo();
                    break;
                case 2:
                    contarMensajesPorEntidad();
                    break;
                case 3:
                    contarCiudadanosPorNivelYSexo();
                    break;
                case 4:
                    calcularEdadPromedioPorNivel();
                    break;
                case 5:
                    System.out.println("Saliendo...");
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    private static void contarMensajesPorSexo() {
        Map<Character, Integer> conteo = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Curps.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                char sexo = linea.split(",")[1].charAt(10);
                conteo.put(sexo, conteo.getOrDefault(sexo, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Mensajes por sexo:");
        System.out.println("Hombres: " + conteo.getOrDefault('H', 0));
        System.out.println("Mujeres: " + conteo.getOrDefault('M', 0));
    }

    private static void contarMensajesPorEntidad() {
        Map<String, Integer> conteo = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Curps.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String entidad = linea.split(",")[1].substring(11, 13);
                conteo.put(entidad, conteo.getOrDefault(entidad, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Mensajes por entidad federativa:");
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static void contarCiudadanosPorNivelYSexo() {
        System.out.print("Ingrese el nivel de estudios: ");
        Scanner scanner = new Scanner(System.in);
        String nivel = scanner.nextLine();

        Map<Character, Integer> conteo = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Curps.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes[2].equals(nivel)) {
                    char sexo = partes[1].charAt(10);
                    conteo.put(sexo, conteo.getOrDefault(sexo, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Ciudadanos con nivel " + nivel + " por sexo:");
        System.out.println("Hombres: " + conteo.getOrDefault('H', 0));
        System.out.println("Mujeres: " + conteo.getOrDefault('M', 0));
    }

    private static void calcularEdadPromedioPorNivel() {
        System.out.print("Ingrese el nivel de estudios: ");
        Scanner scanner = new Scanner(System.in);
        String nivel = scanner.nextLine();

        int sumaEdades = 0;
        int conteo = 0;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        try (BufferedReader br = new BufferedReader(new FileReader("Curps.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes[2].equals(nivel)) {
                    int yearNacimiento = Integer.parseInt("19" + partes[1].substring(4, 6));  // Asumiendo que todos nacieron en el siglo XX
                    int edad = currentYear - yearNacimiento;
                    sumaEdades += edad;
                    conteo++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double promedio = conteo > 0 ? (double) sumaEdades / conteo : 0;
        System.out.println("Edad promedio de ciudadanos con nivel " + nivel + ": " + promedio);
    }
}
