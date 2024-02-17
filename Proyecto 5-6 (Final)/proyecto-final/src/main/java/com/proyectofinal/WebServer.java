/*
 * Proyecto Final
 * González González Jesús Asael
 * 7CM2
 */

package com.proyectofinal;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;



public class WebServer {
   
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/resources/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";
    private static final String METRICS_ENDPOINT = "/metrics";

    private int port = 8089; 
    private HttpServer server; 
    private final ObjectMapper objectMapper;

    public static void main(String[] args) {
        int port = 8089; // O el puerto que prefieras
        WebServer webServer = new WebServer(port);
        webServer.startServer();
        System.out.println("WebServer iniciado en el puerto " + port);
    }

    public WebServer(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void startServer() {
        try {
            // Intenta crear un servidor HTTP en el puerto especificado (port). 
            // El argumento 0 para la segunda opción indica que se utilizará la cola de conexión predeterminada.
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            // En caso de un error de E/S (como un puerto ya en uso), imprime la traza del error y sale de la función.
            e.printStackTrace();
            return;
        }
    
        // Crea un contexto para manejar solicitudes HTTP en la ruta "/status".
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT); 
    
        // Crea un contexto para manejar solicitudes en la ruta "/procesar_datos".
        HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);
    
        // Crea un contexto para la página de inicio o raíz del servidor ("/").
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
    
        // Crea un contexto para manejar solicitudes en la ruta "/metrics".
        HttpContext metricsContext = server.createContext(METRICS_ENDPOINT);
    
        // Asigna un manejador para procesar las solicitudes de estado.
        statusContext.setHandler(this::handleStatusCheckRequest);
    
        // Asigna un manejador para procesar las tareas (solicitudes POST, por ejemplo).
        taskContext.setHandler(this::handleTaskRequest);
    
        // Asigna un manejador para solicitudes de recursos estáticos (por ejemplo, archivos HTML/CSS/JS).
        homePageContext.setHandler(this::handleRequestForAsset);
    
        // Asigna un manejador para las solicitudes de métricas del sistema.
        metricsContext.setHandler(this::handleMetricsRequest);
    
        // Establece un grupo de hilos con un número fijo de hilos (8 en este caso) para manejar las solicitudes.
        server.setExecutor(Executors.newFixedThreadPool(8));
    
        // Inicia el servidor, lo que permite que comience a aceptar solicitudes entrantes.
        server.start();
    }
    
    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
        // Registra información sobre qué recurso está siendo solicitado para propósitos de depuración.
        Logger.getLogger(WebServer.class.getName()).info("Manejando solicitud de activo: " + exchange.getRequestURI());
    
        // Verifica si el método de la solicitud HTTP es GET; si no lo es, cierra la conexión.
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
    
        byte[] response;
        // Obtiene el camino del recurso solicitado a partir de la URL de la solicitud.
        String asset = exchange.getRequestURI().getPath();
    
        // Verifica si el recurso solicitado es la página de inicio (HOME_PAGE_ENDPOINT).
        // Si es así, lee el archivo 'index.html' de los recursos estáticos.
        if (asset.equals(HOME_PAGE_ENDPOINT)) {
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
        } else {
            // Si no es la página de inicio, lee el recurso solicitado.
            response = readUiAsset(asset);
        }
    
        // Agrega el tipo de contenido apropiado a la cabecera de la respuesta HTTP basado en el tipo de archivo solicitado.
        addContentType(asset, exchange);
    
        // Envía la respuesta HTTP con los bytes del recurso leído.
        sendResponse(response, exchange);
    }
    

    private void handleMetricsRequest(HttpExchange exchange) {
        try {
            // Comprueba si el método de la solicitud HTTP es GET; si no, cierra la conexión.
            if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
                exchange.close();
                return;
            }
    
            // Obtiene las métricas del sistema actual (como uso de CPU y memoria).
            Map<String, Double> systemMetrics = getSystemMetrics();
    
            // Convierte las métricas a un objeto JSON para facilitar la respuesta.
            JSONObject jsonResponse = new JSONObject(systemMetrics);
    
            // Convierte el objeto JSON a bytes para la respuesta HTTP.
            byte[] responseBytes = jsonResponse.toString().getBytes();
    
            // Añade cabeceras HTTP para permitir solicitudes de cualquier origen y establecer el tipo de contenido como JSON.
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
    
            // Envía una respuesta HTTP 200 (OK) con los bytes de las métricas.
            exchange.sendResponseHeaders(200, responseBytes.length);
    
            // Escribe los bytes en el cuerpo de la respuesta y los envía.
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // Imprime el error en caso de excepción de entrada/salida.
            e.printStackTrace(); 
        } finally {
            // Asegura que el intercambio HTTP se cierra en todos los casos.
            exchange.close();
        }
    }
    
    

    private byte[] readUiAsset(String asset) throws IOException {
        // Obtiene un InputStream para el recurso especificado por la ruta 'asset'.
        // El recurso debe estar en el mismo classpath que esta clase.
        InputStream assetStream = getClass().getResourceAsStream(asset);
    
        // Si el InputStream es nulo, significa que el recurso no se encontró.
        // En ese caso, retorna un arreglo de bytes vacío.
        if (assetStream == null) {
            return new byte[]{};
        }
    
        // Si el recurso se encontró, lee todos los bytes del InputStream.
        // Esto es útil para leer archivos como HTML, CSS o JavaScript, que se
        // incluirán como parte de la respuesta HTTP.
        return assetStream.readAllBytes(); 
    }
    

    private static void addContentType(String asset, HttpExchange exchange) {
        // Establece un valor predeterminado para el tipo de contenido como "text/html".
        // Esto se utiliza si el tipo de archivo no se identifica específicamente a continuación.
        String contentType = "text/html";
    
        // Comprueba si la extensión del archivo es "js" (JavaScript).
        // Si es así, establece el tipo de contenido como "text/javascript".
        if (asset.endsWith("js")) {
            contentType = "text/javascript";
        } 
        // Comprueba si la extensión del archivo es "css" (Cascading Style Sheets).
        // Si es así, establece el tipo de contenido como "text/css".
        else if (asset.endsWith("css")) {
            contentType = "text/css";
        }
    
        // Añade una cabecera "Content-Type" a la respuesta HTTP con el tipo de contenido adecuado.
        // Esto asegura que el navegador interprete correctamente el tipo del recurso que se está enviando.
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }
    

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        // Añade cabeceras para permitir solicitudes CORS (Cross-Origin Resource Sharing) desde cualquier origen.
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    
        // Maneja las solicitudes de tipo "OPTIONS" que son parte del protocolo CORS.
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1); // 204 No Content, la solicitud OPTIONS no necesita contenido.
            exchange.close(); // Cierra la conexión después de enviar la respuesta.
            return; // Termina la ejecución de la función.
        }
        
        // Comprueba si la solicitud es de tipo "POST".
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.close(); // Cierra la conexión si no es una solicitud POST.
            return; // Termina la ejecución de la función.
        }
        
        try {
            // Registra un mensaje para depurar, indicando que se ha recibido una solicitud POST.
            Logger.getLogger(WebServer.class.getName()).info("Solicitud POST recibida en handleTaskRequest");
    
            // Lee el cuerpo de la solicitud, lo deserializa y obtiene la frase para buscar.
            FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class); 
            String frase = frontendSearchRequest.getSearchQuery();
            System.out.println("Iniciando solicitudes a ServidorLibros para la frase: '" + frase + "'");
    
            // Registra un mensaje para depurar, mostrando la frase recibida.
            Logger.getLogger(WebServer.class.getName()).info("Frase recibida: '" + frase + "'");
    
            // Envía la frase al servidor de libros y recibe la respuesta.
            String responseFromServidorLibros = sendPhraseToServidorLibros(frase);
    
            // Envía la respuesta obtenida al cliente.
            sendResponse(responseFromServidorLibros.getBytes(), exchange);
        } catch (IOException e) {
            e.printStackTrace(); // Imprime detalles del error si ocurre una excepción de E/S.
            return; // Termina la ejecución de la función en caso de error.
        }
    }
    


    private Map<String, Double> getSystemMetrics() throws IOException {
        // Obtiene el ID del proceso (PID) del proceso actual (es decir, de esta instancia de Java).
        long pid = ProcessHandle.current().pid();
    
        // Inicializa un mapa para almacenar métricas del sistema, en este caso, uso de CPU y memoria.
        Map<String, Double> metrics = new HashMap<>();
    
        // Define un comando para el sistema operativo que obtiene el uso de CPU y memoria para el proceso actual.
        // 'ps' es un comando de UNIX para ver información sobre procesos en ejecución.
        // '-p' especifica el PID del proceso a observar.
        // '-o %cpu,%mem' indica que se desea obtener el porcentaje de uso de CPU y memoria.
        String command = "ps -p " + pid + " -o %cpu,%mem";
    
        // Ejecuta el comando en el sistema operativo.
        Process process = Runtime.getRuntime().exec(command);
    
        // Utiliza un BufferedReader para leer la salida del comando ejecutado.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            // Lee cada línea de la salida del comando.
            while ((line = reader.readLine()) != null) {
                // Ignora la primera línea que contiene las cabeceras '%CPU' y '%MEM'.
                if (line.contains("%CPU")) continue;
    
                // Separa la línea en partes usando espacios como delimitadores.
                String[] parts = line.trim().split("\\s+");
                // Si la línea contiene dos partes, asume que son los porcentajes de CPU y memoria.
                if (parts.length == 2) {
                    // Convierte los porcentajes de CPU y memoria a números decimales y los almacena en el mapa.
                    metrics.put("cpuUsage", Double.parseDouble(parts[0]));
                    metrics.put("memoryUsage", Double.parseDouble(parts[1]));
                }
            }
        }
    
        // Devuelve el mapa con las métricas recopiladas.
        return metrics;
    }
    
    

    private String sendPhraseToServidorLibros(String phrase) {
        // Imprime un mensaje indicando que se están iniciando las solicitudes a los servidores de libros.
        System.out.println("Iniciando solicitudes a ServidorLibros para la frase: '" + phrase + "'");
    
        // Crea una lista de tareas asíncronas (futures) para enviar solicitudes a varios servidores de libros.
        // Cada servidor se ejecuta en un puerto diferente (8081, 8082, 8083).
        List<CompletableFuture<String>> futures = List.of(
            sendRequestToServidorLibros(phrase, 8081),
            sendRequestToServidorLibros(phrase, 8082),
            sendRequestToServidorLibros(phrase, 8083)
        );
    
        // Espera a que todas las tareas asíncronas se completen y recopila los resultados.
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<String>> allPageContentsFuture = allFutures.thenApply(v ->
            futures.stream()
                   .map(CompletableFuture::join) // Espera a que cada tarea asíncrona se complete y obtiene su resultado.
                   .collect(Collectors.toList()) // Recopila los resultados en una lista.
        );
    
        // Combinar los resultados de todas las solicitudes en un solo JSON.
        try {
            // Obtiene los resultados combinados.
            List<String> results = allPageContentsFuture.get();
            // Crea un objeto JSON para almacenar los resultados combinados.
            JSONObject combinedResults = new JSONObject();
            // Itera sobre cada resultado y los combina en un solo JSON.
            for (String result : results) {
                // Convierte cada resultado a un objeto JSON.
                JSONObject jsonResult = new JSONObject(result);
                // Añade cada par clave-valor del resultado al objeto JSON combinado.
                jsonResult.keys().forEachRemaining(key -> {
                    combinedResults.put(key, jsonResult.get(key));
                });
            }
            // Convierte el objeto JSON combinado a una cadena y la devuelve.
            return combinedResults.toString();
        } catch (Exception e) {
            // En caso de un error, imprime la traza del error y devuelve un mensaje de error.
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    private CompletableFuture<String> sendRequestToServidorLibros(String phrase, int port) {
        // Construye la URL para la solicitud al servidor de libros, incluyendo la frase codificada para URLs.
        String url = "http://localhost:" + port + "/searchphrase?phrase=" + URLEncoder.encode(phrase, StandardCharsets.UTF_8);
    
        // Imprime un mensaje en la consola indicando que se está enviando una solicitud al servidor de libros en el puerto especificado.
        System.out.println("Enviando solicitud a ServidorLibros en puerto " + port + " con frase: '" + phrase + "'");
    
        // Crea un cliente HTTP para enviar la solicitud.
        HttpClient client = HttpClient.newHttpClient();
        // Construye la solicitud HTTP con el método GET y la URL creada.
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
    
        // Envía la solicitud de forma asíncrona y devuelve un CompletableFuture.
        // El CompletableFuture se completará cuando se reciba la respuesta del servidor.
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                     // Una vez que se recibe la respuesta, extrae el cuerpo de la respuesta y lo devuelve como una cadena.
                     .thenApply(HttpResponse::body)
                     // En caso de que ocurra una excepción durante la solicitud, devuelve un mensaje de error.
                     .exceptionally(e -> "Error: " + e.getMessage());
    }
    
    

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        // Verifica si el método de la solicitud HTTP es GET.
        // Si el método no es GET, cierra la conexión y termina la ejecución de la función.
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }
    
        // Define un mensaje de respuesta que indica que el servidor está activo y funcionando.
        String responseMessage = "El servidor está vivo\n";
    
        // Envía el mensaje de respuesta al cliente que hizo la solicitud.
        // Convierte el mensaje de respuesta a un array de bytes, ya que es el formato requerido para el cuerpo de la respuesta HTTP.
        sendResponse(responseMessage.getBytes(), exchange);
    }
    

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        // Configura los encabezados de la respuesta HTTP y envía un código de estado 200, que indica éxito.
        // El segundo parámetro es la longitud del cuerpo de la respuesta, que es la longitud del array de bytes de la respuesta.
        exchange.sendResponseHeaders(200, responseBytes.length);
    
        // Obtiene el cuerpo de la respuesta HTTP como un OutputStream.
        OutputStream outputStream = exchange.getResponseBody();
    
        // Escribe el array de bytes de la respuesta en el OutputStream.
        // Esto envía efectivamente los datos al cliente que hizo la solicitud.
        outputStream.write(responseBytes);
    
        // Vacía cualquier dato restante en el buffer del OutputStream y lo envía al cliente.
        outputStream.flush();
    
        // Cierra el OutputStream, lo que finaliza la respuesta y cierra la conexión.
        outputStream.close();
    }
    
}
