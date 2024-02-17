package com.mycompany.proyecto3;

import java.util.*;
import java.io.*;

class Proyecto3 {
 public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    
    limpiarArchivo();  // Limpia el archivo al inicio
    
    System.out.println("Ingrese el número de registros a generar por segundo: ");
    int num = scanner.nextInt();
    scanner.nextLine();  // Consumir el salto de línea pendiente
    
    while(true) {
        for(int i = 0; i < num; i++) {
            String registro = generarRegistro();
            escribirRegistro(registro);
        }
        try {
            Thread.sleep(1000);  // Dormir por un segundo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

    
    static void limpiarArchivo() {
    try (FileWriter fw = new FileWriter("Curps.txt");
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw)) {
        out.print("");  // Escribe una cadena vacía para limpiar el archivo
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    
    static String generarRegistro() {
        String[] niveles = {"PREESCOLAR", "PRIMARIA", "SECUNDARIA", "PREPARATORIA", "UNIVERSIDAD", "MAESTRIA", "DOCTORADO"};
        String curp = getCURP();
        String nivel = niveles[new Random().nextInt(niveles.length)];
        String numeroCelular = "55" + (int)(Math.random() * 10000000 + 10000000);  // Generar un número de 10 dígitos
        return numeroCelular + "," + curp + "," + nivel + "\n";
    }
    
    static void escribirRegistro(String registro) {
        try (FileWriter fw = new FileWriter("Curps.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(registro);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
      static String getCURP() {
        String Letra = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String Numero = "0123456789";
        String Sexo = "HM";
        String Entidad[] = {"AS", "BC", "BS", "CC", "CS", "CH", "CL", "CM", "DF", "DG", "GT", "GR", "HG", "JC", "MC", "MN", "MS", "NT", "NL", "OC", "PL", "QT", "QR", "SP", "SL", "SR", "TC", "TL", "TS", "VZ", "YN", "ZS"};
        
        StringBuilder sb = new StringBuilder(18);
        
        for (int i = 1; i < 5; i++) {
            int indice = (int) (Letra.length()* Math.random());
            sb.append(Letra.charAt(indice));        
        }
        
        for (int i = 5; i < 11; i++) {
            int indice = (int) (Numero.length()* Math.random());
            sb.append(Numero.charAt(indice));        
        }
        int indice = (int) (Sexo.length()* Math.random());
        sb.append(Sexo.charAt(indice));        
        sb.append(Entidad[(int)(Math.random()*32)]);
        
        for (int i = 14; i < 17; i++) {
            indice = (int) (Letra.length()* Math.random());
            sb.append(Letra.charAt(indice));        
        }
        
        for (int i = 17; i < 19; i++) {
            indice = (int) (Numero.length()* Math.random());
            sb.append(Numero.charAt(indice));        
        }
        
        return sb.toString();
    }
}
