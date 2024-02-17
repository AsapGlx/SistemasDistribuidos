/*
 * Proyecto Final
 * González González Jesús Asael
 * 7CM2
 */

package com.proyectofinal;

import java.io.IOException;
public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Establece el puerto del servidor web en 8089 como valor predeterminado.
        int currentServerPort = 8089;
        
        // Si se proporciona un argumento en la línea de comandos, lo utiliza como puerto en lugar del predeterminado.
        if (args.length == 1) {
            currentServerPort = Integer.parseInt(args[0]);
        }
        
        // Crea una instancia de la clase `App`. No se muestra la definición de esta clase en el código proporcionado.
        App application = new App();
        
        // Crea una instancia de la clase `WebServer` con el puerto especificado.
        WebServer webServer = new WebServer(currentServerPort);
        
        // Inicia el servidor web.
        webServer.startServer();
        
        // Muestra un mensaje en la consola indicando en qué puerto está escuchando el servidor.
        System.out.println("Servidor escuchando en el puerto: " + currentServerPort);
    }
    
}