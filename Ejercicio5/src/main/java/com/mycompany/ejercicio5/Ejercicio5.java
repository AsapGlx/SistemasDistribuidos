
package com.mycompany.ejercicio5;

import java.util.Random;
import java.util.Scanner;

public class Ejercicio5 {

    
    
    public static void main(String[] args) {
      Scanner scanner = new Scanner(System.in);
        System.out.print("Ingresa el valor de n: ");
        int n = scanner.nextInt();

        char[] cadenota = generateCadenota(n);

        int count = countOccurrences(cadenota, "IPN");
        System.out.println("Número de ocurrencias de 'IPN': " + count);
    }

    
    
    public static char[] generateCadenota(int n) {
        Random rand = new Random();
        
        char[] cadenota = new char[n * 4];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 3; j++) {
                cadenota[i * 4 + j] = (char) ('A' + rand.nextInt(26));
            }
            cadenota[i * 4 + 3] = ' ';
        }
        return cadenota;
    }

    
    
    public static int countOccurrences(char[] cadenota, String target) {
        int count = 0;
        for (int i = 0; i < cadenota.length - target.length(); i++) {
            boolean found = true;           
            for (int j = 0; j < target.length(); j++) {
                if (cadenota[i + j] != target.charAt(j)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                count++;
                System.out.println("Ocurrencia encontrada en la posición: " + i);
            }
        }
        return count;
    }
}

