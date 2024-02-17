
package com.mycompany.lecturatxt;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.HashMap; 
import java.util.Map; 
import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.IOException; 
 
class Task implements Runnable { 
 private String name; 
 private int noLinea; 
 private String nombreArchivo = "C:\\Users\\cuyob\\OneDrive\\Escritorio\\Proyecto\\BIBLIA_COMPLETA.txt"; 
 
 public Task(String s, int l) { 
 name = s; 
 noLinea = l; 
 } 
 
 public void run() { 
 Map<Character, Integer> mapa = new HashMap<>(); 
 try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) { 
 String linea; 
 int contLinea = 0; 
 
 while (contLinea < noLinea) { 
 if (br.readLine() == null) 
 break; 
 contLinea++; 
 } 
 
 while ((linea = br.readLine()) != null && contLinea < noLinea + 7037) { 
 for (char letra : linea.toCharArray()) { 
 if (!mapa.containsKey(letra)) { 
 mapa.put(letra, 1); 
 } else { 
 int valor = mapa.get(letra); 
 valor += 1; 
 mapa.put(letra, valor); 
 } 
 } 
 contLinea++; 
 } 
 
 System.out.println(name + "\n" + mapa); 
 try { 
 Thread.sleep(1000); 
 } catch (InterruptedException e) { 
 e.printStackTrace(); 
 } 
 } catch (IOException e) { 
 e.printStackTrace(); 
 } 
 } 
} 
 
public class BibliaMap { 
 static final int MAX_T = 5; 
 
 public static void main(String[] args) { 
 Runnable r1 = new Task("Mapa 1", 0); 
 Runnable r2 = new Task("Mapa 2", 7037); 
 Runnable r3 = new Task("Mapa 3", 14074); 
 //Runnable r4 = new Task("Mapa 4", 21111); 
 //Runnable r5 = new Task("Mapa 5", 28148); 
 
 ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 
 
 pool.execute(r1); 
 pool.execute(r2); 
 pool.execute(r3); 
 //pool.execute(r4); 
 //pool.execute(r5); 
 
 pool.shutdown(); 
 } 
}