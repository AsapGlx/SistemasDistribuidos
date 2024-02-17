/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.mycompany.clientezookeepereleccion;

import org.apache.zookeeper.*;
import java.io.IOException;

//Implementacion de Watcher, permite capturar los eventos proveninetes del servidor ZooKeeper

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ClienteZooKeeperEleccion implements Watcher {

    //Direcion en la que se encuentra nuestro servidor ZooKeeper
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    //Timeout en unidades de milisegundos, verificar la comunicacion
    private static final int SESSION_TIMEOUT = 3000;
    //Objeto de la clase ZooKeeper para interacturar con el servidor con sus metodos
    private ZooKeeper zooKeeper;
    private static final String ELECTION_NAMESPACE = "/eleccion"; 

    private String currentZnodeName; 

    //Manejo de excepciones a la maquina virtual de java, InterruptedException permite trabajar con eventos de los hilos de ZooKeeper
    // KeeperException maneja las excepciones propias de ZooKeeper
    public static void main(String[] arg) throws IOException, InterruptedException, KeeperException {
        //Instancia de nuestra clase con watcher
        ClienteZooKeeperEleccion clienteBasico = new ClienteZooKeeperEleccion();
        //Se ejecuta el metodo connetToZooKeeper
        clienteBasico.connectToZookeeper();
        clienteBasico.voluntarioParaLiderazgo();
        clienteBasico.liderElecto();
        //Metodo run
        clienteBasico.run();
        //Metodo close
        clienteBasico.close();
    }

    public void connectToZookeeper() throws IOException {
        //Instanciamos un objeto con la informacion de ubicacion y el timeout
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void run() throws InterruptedException {
        //Permite a un solo hilo ejecutar la instruccion zooKeeper.wait
        synchronized (zooKeeper) {
            //Deja dicho hilo en espera hasta que llegue una notificacion 
            //El hilo principal se queda en espera hasta que no se cierre la conexion con el servidor
            //Si no se ejecuta la siguiente instruccion 
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    @Override
    //Recibe los eventos producidos por el servidor ZooKeeper mediante objetos tipo WatchedEvent
    public void process(WatchedEvent event) {
        //Determinar el tipo de evento 
        switch (event.getType()) {
            case None:
                //Obtener estado del evento usamos getState
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    //Si corresponde a Event.KeeperState.SyncConnected significa que estamos conectados
                    System.out.println("Conectado exitosamente a Zookeeper");
                } else {
                    //Logra la exclusion mutua 
                    synchronized (zooKeeper) {
                        //Si llega de tipo none pero no esta conectado entonces
                        //Significa que esta desconectado
                        System.out.println("Desconectando de Zookeeper...");
                        //Despierta a todos los hilos en espera
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
    
    
    public void voluntarioParaLiderazgo() throws KeeperException, InterruptedException { 

        String znodePrefix = ELECTION_NAMESPACE + "/c_"; 

        String znodeFullPath = zooKeeper.create(znodePrefix, new byte[]{},ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);         

        System.out.println("Nombre del znode: " + znodeFullPath); 

        this.currentZnodeName = znodeFullPath.replace("/eleccion/", ""); 

    }
    
    
    
    public void liderElecto() throws KeeperException, InterruptedException { 

       List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false); 

       Collections.sort(children); 

       String smallestChild = children.get(0); 

 

       if (smallestChild.equals(currentZnodeName)) { 

          System.out.println("Yo soy el lider"); 

          return; 

       } 

       System.out.println("Yo no soy el lider, " + smallestChild + " es el lider"); 

    } 


}



