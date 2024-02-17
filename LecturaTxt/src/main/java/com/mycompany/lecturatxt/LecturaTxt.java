package com.mycompany.lecturatxt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LecturaTxt {
    public static void main(String[] args) {
        // Ruta al archivo
        String filePath = "C:\\Users\\cuyob\\OneDrive\\Escritorio\\Proyecto\\BIBLIA_COMPLETA.txt";
        
        // Map para almacenar caracteres y sus ocurrencias
        Map<Character, Integer> charCountMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int currentChar;
            while ((currentChar = reader.read()) != -1) {
                // Convertir int a char
                char character = (char) currentChar;
                
                // Actualizar el Map
                charCountMap.put(character, charCountMap.getOrDefault(character, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Convertir el Map a una lista de Map.Entry
        List<Map.Entry<Character, Integer>> entryList = new ArrayList<>(charCountMap.entrySet());
        
        // Ordenar la lista por valor de ocurrencia de menor a mayor
        entryList.sort(Comparator.comparing(Map.Entry::getValue));
        
        // Imprimir resultados
        System.out.println("Número de caracteres distintos encontrados: " + charCountMap.size());
        System.out.println("Ocurrencias de cada carácter ordenadas de menor a mayor:");
        for (Map.Entry<Character, Integer> entry : entryList) {
            System.out.println("Carácter: " + entry.getKey() + ", Ocurrencias: " + entry.getValue());
        }
    }
}
