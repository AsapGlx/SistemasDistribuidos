/*
 * Proyecto Final
 * González González Jesús Asael
 * 7CM2
 */

package com.proyectofinal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ServidorLibros {

    private static final String PHRASE_SEARCH_ENDPOINT = "/searchphrase";
    private static final String METRICS_ENDPOINT = "/metrics";
    private final int port;
    
    private HttpServer server;
    private Map<String, Map<String, Double>> detailedBookScores = new HashMap<>();
    
    static int numberOfServers = 3; // Total de servidores
    private final int serverIndex;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java ServidorLibros <puerto> <índice del servidor>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        int serverIndex = Integer.parseInt(args[1]);
    
        ServidorLibros webServer = new ServidorLibros(port, serverIndex);
        webServer.startServer();
        System.out.println("Servidor " + serverIndex + " escuchando en el puerto " + port);
    }
    
    

    public ServidorLibros(int port, int serverIndex) {
        this.port = port;
        this.serverIndex = serverIndex; // Asigna el índice del servidor
    }

    public void startServer() {
        try {
            // Crea una instancia del servidor HTTP escuchando en el puerto especificado.
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
    
            // Crea un contexto HTTP para el endpoint de búsqueda de frases. 
            // Esto significa que el servidor manejará las solicitudes HTTP que lleguen a este endpoint.
            HttpContext phraseSearchContext = server.createContext(PHRASE_SEARCH_ENDPOINT);
            // Asigna el manejador (handler) para el contexto de búsqueda de frases.
            // Cuando una solicitud llegue a este contexto, se invocará el método handlePhraseSearchRequest.
            phraseSearchContext.setHandler(this::handlePhraseSearchRequest);
    
            // Crea un contexto similar para el endpoint de métricas del servidor.
            HttpContext metricsContext = server.createContext(METRICS_ENDPOINT);
            // Asigna el manejador para el contexto de métricas.
            metricsContext.setHandler(this::handleMetricsRequest);
    
            // Establece un pool de hilos fijos para manejar las solicitudes concurrentes.
            // En este caso, se permite un máximo de 8 solicitudes concurrentes.
            server.setExecutor(Executors.newFixedThreadPool(8));
            
            // Inicia el servidor. Este método retorna inmediatamente después de iniciar.
            server.start();
        } catch (IOException e) {
            // Captura cualquier excepción de Entrada/Salida que ocurra al intentar iniciar el servidor
            // e imprime la traza de la pila para depuración.
            e.printStackTrace();
        }
    }


    public static Map<String, Double> getMetricsForPid() throws IOException {
        // Obtiene el ID del proceso actual.
        long pid = ProcessHandle.current().pid();
    
        // Crea un mapa para almacenar las métricas de CPU y memoria.
        Map<String, Double> metrics = new HashMap<>();
    
        // Construye un comando shell para obtener el uso de CPU y memoria del proceso.
        // 'ps -p' especifica el proceso por PID, '-o %cpu,%mem' define los datos a mostrar: uso de CPU y memoria.
        String command = "ps -p " + pid + " -o %cpu,%mem";
    
        // Ejecuta el comando en el entorno de tiempo de ejecución.
        Process process = Runtime.getRuntime().exec(command);
    
        // Utiliza un BufferedReader para leer la salida del comando ejecutado.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            // Lee cada línea de la salida del comando.
            while ((line = reader.readLine()) != null) {
                // Ignora la línea de cabecera que contiene "%CPU".
                if (line.contains("%CPU")) continue;
    
                // Divide la línea en componentes separados por espacios.
                // Se espera que haya dos partes: uso de CPU y uso de memoria.
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    // Convierte las partes en valores double y las almacena en el mapa.
                    // 'cpuUsage' y 'memoryUsage' son las claves para acceder a estos valores más adelante.
                    metrics.put("cpuUsage", Double.parseDouble(parts[0]));
                    metrics.put("memoryUsage", Double.parseDouble(parts[1]));
                }
            }
        }
    
        // Devuelve el mapa que contiene las métricas de uso de CPU y memoria del proceso.
        return metrics;
    }    

    private void handlePhraseSearchRequest(HttpExchange exchange) throws IOException {
        // Imprime un mensaje en la consola indicando que se ha recibido una solicitud en el puerto especificado.
        System.out.println("Solicitud recibida en el servidor en puerto " + this.port);
    
        // Añade cabeceras a la respuesta HTTP para permitir solicitudes de origen cruzado (CORS).
        // Esto es necesario para que páginas web alojadas en otros dominios puedan hacer solicitudes a este servidor.
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // Establece el tipo de contenido de la respuesta como JSON.
        exchange.getResponseHeaders().add("Content-Type", "application/json");
    
        // Verifica si el método de la solicitud HTTP es GET.
        // Si no lo es, cierra la solicitud y termina el método.
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
    
        // Obtiene la consulta (query) de la URL de la solicitud.
        String query = exchange.getRequestURI().getQuery();
        // Extrae la frase de búsqueda de la consulta, reemplazando los signos '+' con espacios.
        // Esto asume que la frase de búsqueda está en el formato "searchphrase=la+frase+a+buscar".
        String phrase = query.split("=")[1].replace("+", " ");
        // Divide la frase en palabras individuales.
        String[] words = phrase.split("\\s+");
    
        // Realiza la búsqueda de las palabras en los libros.
        // searchWordsInBooks es un método que no se muestra aquí, pero se asume que busca las palabras en una colección de libros y devuelve un mapa con las puntuaciones correspondientes.
        Map<String, Double> bookScores = searchWordsInBooks(words);
    
        // Envía la respuesta con los resultados de la búsqueda.
        // sendResponse es otro método que formatea y envía los resultados como una respuesta JSON a través del HttpExchange.
        sendResponse(bookScores, exchange);
    }
    

    private void handleMetricsRequest(HttpExchange exchange) {
        try {
            // Verifica si el método de la solicitud HTTP es GET.
            // Si no lo es, cierra la solicitud y termina el método.
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.close();
                return;
            }
    
            // Obtiene las métricas del sistema para el proceso actual.
            // Esto incluye el uso de CPU y memoria.
            Map<String, Double> systemMetrics = getMetricsForPid();
    
            // Convierte las métricas obtenidas en un objeto JSON.
            JSONObject jsonResponse = new JSONObject(systemMetrics);
    
            // Convierte el objeto JSON en una cadena y luego en un array de bytes para la respuesta HTTP.
            byte[] responseBytes = jsonResponse.toString().getBytes();
    
            // Agrega cabeceras para permitir solicitudes CORS y establecer el tipo de contenido como JSON.
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
    
            // Envía la respuesta HTTP con un código de estado 200 (OK) y la longitud del contenido.
            exchange.sendResponseHeaders(200, responseBytes.length);
    
            // Obtiene el OutputStream del HttpExchange y escribe los bytes de la respuesta.
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.flush(); // Limpia el stream asegurándose de que todos los datos se envíen.
            outputStream.close(); // Cierra el stream.
        } catch (IOException e) {
            // Imprime el error en caso de una excepción de entrada/salida.
            e.printStackTrace();
        } finally {
            // Asegura que el HttpExchange se cierra en todos los casos al final del manejo de la solicitud.
            exchange.close();
        }
    }
    
    
    private Map<String, Double> searchWordsInBooks(String[] words) {
        // Define el directorio donde se encuentran los archivos de los libros.
        File folder = new File("/home/asapglx/Desktop/profin/proyecto-final/src/main/resources/libros");
        // Lista todos los archivos en el directorio de libros.
        File[] listOfFiles = folder.listFiles();
        // Crea un mapa sincronizado para almacenar los puntajes de los libros, lo que permite su uso en un entorno multihilo.
        Map<String, Double> bookScores = Collections.synchronizedMap(new HashMap<>());
        
        // Calcula los puntajes IDF (Frecuencia Inversa de Documento) para las palabras dadas en el conjunto de archivos.
        Map<String, Double> idfScores = calculateIDF(words, listOfFiles);
        
        // Crea un servicio de ejecución con un pool de hilos fijos para procesar los archivos en paralelo.
        ExecutorService executor = Executors.newFixedThreadPool(8);
        
        // Filtra los archivos a ser procesados por este servidor en particular, basándose en el hash del archivo y el índice del servidor.
        File[] filesToProcess = Arrays.stream(listOfFiles)
                                      .filter(file -> (file.hashCode() & Integer.MAX_VALUE) % numberOfServers == serverIndex)
                                      .toArray(File[]::new);
    
        // Itera sobre cada archivo asignado a este servidor y lo procesa en un hilo separado.
        for (File file : filesToProcess) {
            executor.submit(() -> {
                // Calcula los puntajes de las palabras para el archivo actual.
                Double wordScores = processFile(file, words, idfScores);
                // Almacena el puntaje total del libro en el mapa de puntajes de libros.
                bookScores.put(file.getName(), wordScores);
            });
        }
    
        // Inicia el proceso de cierre del executor, no permitiendo que se envíen nuevas tareas pero completando las existentes.
        executor.shutdown();
        // Espera hasta un minuto para que todas las tareas se completen.
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // Forza el cierre del executor si las tareas no se completan en el tiempo dado.
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // En caso de interrupción, fuerza el cierre del executor y restablece el estado de interrupción del hilo.
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    
        // Devuelve el mapa con los puntajes de los libros.
        return bookScores;
    }
    
    
    private double processFile(File file, String[] words, Map<String, Double> idfScores) {
        // Crea un mapa para almacenar los puntajes TF-IDF de cada palabra en el archivo.
        Map<String, Double> wordScores = new HashMap<>();
        // Inicializa el puntaje total del archivo a 0.
        double score = 0;
        // Cuenta el número total de palabras en el archivo.
        int totalWordsInBook = countTotalWordsInFile(file);
    
        // Itera sobre cada palabra en la lista de palabras buscadas.
        for (String word : words) {
            // Cuenta la frecuencia de la palabra en el archivo (frecuencia de término, TF).
            int tf = countWordOccurrencesInFile(word, file);
            // Obtiene el puntaje IDF para la palabra, o 0.0 si no está presente en el mapa idfScores.
            double idf = idfScores.getOrDefault(word, 0.0);
            // Calcula el puntaje TF-IDF para la palabra en el archivo.
            double tfIdf = (tf / (double) totalWordsInBook) * idf;
            // Almacena el puntaje TF-IDF de la palabra en el mapa.
            wordScores.put(word, tfIdf);
            // Suma el puntaje TF-IDF al puntaje total del archivo.
            score += tfIdf;
        }
    
        // Almacena el mapa de puntajes de palabras para el archivo en una estructura global para uso posterior.
        detailedBookScores.put(file.getName(), wordScores);
    
        // Devuelve el puntaje total del archivo.
        return score;
    }

        private boolean containsWord(String word, File file) {
        // Intenta abrir el archivo con un Scanner para leer su contenido.
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            // Itera a través de cada línea del archivo.
            while (scanner.hasNextLine()) {
                // Obtiene la siguiente línea del archivo.
                String line = scanner.nextLine();
                // Comprueba si la línea contiene la palabra buscada.
                // Tanto la línea como la palabra se convierten a minúsculas para hacer una comparación insensible a mayúsculas/minúsculas.
                if (line.toLowerCase().contains(word.toLowerCase())) {
                    // Si la palabra está presente en la línea, retorna true.
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            // Captura la excepción si el archivo no se encuentra y muestra el error.
            e.printStackTrace();
        }
        // Retorna false si la palabra no se encuentra en ninguna línea del archivo.
        return false;
    }
    
private int countOccurrencesInLine(String word, String line) {
    // Divide la línea en palabras individuales usando una expresión regular que coincide con cualquier carácter no alfabético.
    // Esto significa que cualquier signo de puntuación, espacio, etc., actuará como separador de palabras.
    String[] words = line.split("\\W+"); 

    // Inicializa un contador para las ocurrencias de la palabra específica en esta línea.
    int count = 0;

    // Itera sobre cada palabra en la línea dividida.
    for (String w : words) {
        // Compara la palabra actual (w) con la palabra buscada (word),
        // ignorando las diferencias de mayúsculas y minúsculas.
        if (w.equalsIgnoreCase(word)) {
            // Si la palabra actual coincide con la palabra buscada, incrementa el contador.
            count++;
        }
    }

    // Devuelve el número total de veces que la palabra buscada aparece en la línea.
    return count;
}

private int countWordOccurrencesInFile(String word, File file) {
    // Inicializa un contador para las ocurrencias de la palabra en el archivo.
    int count = 0;
            // Crea un Scanner para leer el archivo, usando UTF-8 como codificación de caracteres.
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
           // Itera sobre cada línea del archivo.
        while (scanner.hasNextLine()) {
            // Lee la próxima línea del archivo.
            String line = scanner.nextLine();
            // Utiliza el método countOccurrencesInLine para contar cuántas veces aparece la palabra en esta línea.
            // Suma este número al contador total.
            count += countOccurrencesInLine(word, line);
        }
    } catch (FileNotFoundException e) {
        // En caso de que el archivo no se encuentre, imprime el stack trace del error.
        e.printStackTrace();
    }
    // Devuelve el conteo total de las ocurrencias de la palabra en el archivo.
    return count;
}

    private int countTotalWordsInFile(File file) {
        // Inicializa un contador para las palabras totales en el archivo.
        int count = 0;
        // Crea un Scanner para leer el archivo, usando UTF-8 como codificación de caracteres.
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
         // Itera sobre cada línea del archivo.
        while (scanner.hasNextLine()) {
            // Lee la próxima línea del archivo.
            String line = scanner.nextLine();
            // Divide la línea en palabras basándose en cualquier carácter que no sea una letra (\\W+ es una expresión regular que coincide con cualquier secuencia de caracteres no alfabéticos).
            // Cuenta el número de elementos en el arreglo resultante, que corresponde al número de palabras en la línea.
            count += line.split("\\W+").length;
        }
    } catch (FileNotFoundException e) {
        // En caso de que el archivo no se encuentre, imprime el stack trace del error.
        e.printStackTrace();
    }
    // Devuelve el conteo total de palabras en el archivo.
    return count;
}

private Map<String, Double> calculateIDF(String[] words, File[] listOfFiles) {
        // Crea un mapa para almacenar los puntajes IDF (Inverse Document Frequency) de cada palabra.
        Map<String, Double> idfScores = new HashMap<>();
        // Cuenta el número total de documentos basándose en la longitud del array de archivos proporcionado.
        int totalNumberOfDocuments = listOfFiles.length;
    
        // Itera sobre cada palabra en el array de palabras para calcular su IDF.
        for (String word : words) {
            // Inicializa un contador para la cantidad de documentos que contienen la palabra.
            int numberOfDocumentsContainingWord = 0;
            // Itera sobre cada archivo en el array de archivos.
            for (File file : listOfFiles) {
                // Utiliza el método containsWord para comprobar si el archivo contiene la palabra.
                if (containsWord(word, file)) {
                    // Incrementa el contador si la palabra está contenida en el archivo.
                    numberOfDocumentsContainingWord++;
                }
            }
            // Calcula el IDF utilizando la fórmula logarítmica: log(Total de documentos / Número de documentos que contienen la palabra).
            // Esto mide la importancia de una palabra; las palabras más raras obtienen un mayor IDF.
            double idf = Math.log10((double) totalNumberOfDocuments / (numberOfDocumentsContainingWord));
            // Almacena el IDF calculado en el mapa, asociándolo con la palabra correspondiente.
            idfScores.put(word, idf);
        }
    
        // Retorna el mapa con los puntajes IDF de todas las palabras.
        return idfScores;
    }


private void sendResponse(Map<String, Double> bookScores, HttpExchange exchange) throws IOException {
    // Convierte el mapa de puntajes de libros en una lista para poder ordenarlos.
    List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(bookScores.entrySet());
    // Ordena los libros en orden descendente según su puntaje.
    sortedEntries.sort(Map.Entry.<String, Double>comparingByValue().reversed());

    // Crea un objeto JSON para almacenar la respuesta.
    JSONObject jsonResponse = new JSONObject();

    // Itera sobre cada entrada en la lista ordenada de libros.
    for (Map.Entry<String, Double> bookEntry : sortedEntries) {
        // Obtiene el nombre del libro y su puntaje total.
        String bookName = bookEntry.getKey();
        Double score = bookEntry.getValue();

        // Crea un objeto JSON para este libro en particular.
        JSONObject bookDetails = new JSONObject();
        // Agrega el puntaje total del libro al objeto JSON.
        bookDetails.put("scoreTotal", score);

        // Si hay detalles de TF*IDF disponibles para este libro, se agregan también.
        if (detailedBookScores.containsKey(bookName)) {
            // Obtiene los detalles de TF*IDF y los agrega al objeto JSON del libro.
            JSONObject tfidfDetails = new JSONObject(detailedBookScores.get(bookName));
            bookDetails.put("detalles", tfidfDetails);
        }

        // Agrega el objeto JSON del libro a la respuesta JSON global.
        jsonResponse.put(bookName, bookDetails);
    }

    // Convierte la respuesta JSON en una cadena y luego en bytes para su envío.
    byte[] responseBytes = jsonResponse.toString().getBytes();

    // Configura los encabezados de respuesta y envía la respuesta HTTP con los datos JSON.
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, responseBytes.length);
    OutputStream outputStream = exchange.getResponseBody();
    outputStream.write(responseBytes);
    outputStream.flush();
    outputStream.close();
    exchange.close(); // Cierra el intercambio HTTP una vez que la respuesta ha sido enviada.
}


}
