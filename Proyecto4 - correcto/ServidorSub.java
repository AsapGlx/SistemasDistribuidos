import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ServidorSub {
    private static final String SUBTITLE_ENDPOINT = "/subtitle"; // Endpoint para subtítulos
    private static final String STATUS_ENDPOINT = "/status"; // Nuevo endpoint para el estado del servidor
    private static final String MOVIES_ENDPOINT = "/movies";    // Endpoint para listar películas
    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8081;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        ServidorSub webServer = new ServidorSub(serverPort);
        webServer.startServer();
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }

    public ServidorSub(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Crear contexto para el manejo de subtítulos
            HttpContext subtitleContext = server.createContext(SUBTITLE_ENDPOINT);
            subtitleContext.setHandler(this::handleSubtitleRequest);

            // Crear contexto para el estado del servidor
            HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
            statusContext.setHandler(this::handleStatusRequest);

            HttpContext moviesContext = server.createContext(MOVIES_ENDPOINT);
            moviesContext.setHandler(this::handleMoviesRequest);

            server.setExecutor(Executors.newFixedThreadPool(8));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSubtitleRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String query = exchange.getRequestURI().getQuery(); // Ejemplo: "title=Gladiador&number=1"
        String[] queryParams = query.split("&");
        String title = queryParams[0].split("=")[1];
        int number = Integer.parseInt(queryParams[1].split("=")[1]);

        String subtitle = getSubtitle(title, number); // Obtener el subtítulo solicitado

        sendResponse(subtitle.getBytes(), exchange);
    }


    private String getSubtitle(String title, int number) {
        String subtitleFilePath = "/home/asapglx/Desktop/BD/PELICULAS/" + title + ".srt";
        File subtitleFile = new File(subtitleFilePath);
    
        if (!subtitleFile.exists()) {
            return "Subtitle file not found for title: " + title;
        }
    
        StringBuilder subtitleText = new StringBuilder();
        boolean numberFound = false;
    
        try (Scanner scanner = new Scanner(subtitleFile, "UTF-8")) {
            // Saltarse el BOM si está presente
            scanner.useDelimiter("\\A");
            String content = scanner.next();
            content = content.replace("\uFEFF", "");
            
            // Reiniciar el escaneo del contenido
            scanner.close();
            Scanner contentScanner = new Scanner(content);
    
            while (contentScanner.hasNextLine()) {
                String line = contentScanner.nextLine().trim();
                if (line.matches("^\\d+$")) {
                    int currentNumber = Integer.parseInt(line);
                    if (currentNumber == number) {
                        numberFound = true;
                    } else if (numberFound) {
                        // Subtítulo encontrado y completo
                        break;
                    }
                } else if (numberFound) {
                    // Añadir líneas de subtítulo
                    if (line.isEmpty()) {
                        break;
                    }
                    subtitleText.append(line).append("\n");
                }
            }
            contentScanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "Error reading subtitle file for title: " + title;
        }
    
        return numberFound ? subtitleText.toString().trim() : "Subtitle number " + number + " not found";
    }
    
    
    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    private void handleStatusRequest(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String responseText = "Server is running";
            exchange.sendResponseHeaders(200, responseText.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }
        } else {
            String responseText = "Method Not Allowed";
            exchange.sendResponseHeaders(405, responseText.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }
        }
    }

    private String getMoviesList() {
        File folder = new File("/home/asapglx/Desktop/BD/PELICULAS");
        File[] listOfFiles = folder.listFiles();
        StringBuilder movies = new StringBuilder();
    
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".srt")) {
                    String movieName = file.getName().replace(".srt", "");
                    movies.append(movieName).append(",");
                }
            }
        }
    
        return movies.length() > 0 ? movies.toString() : "No movies found";
    }

    private void handleMoviesRequest(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String responseText = getMoviesList();
            exchange.sendResponseHeaders(200, responseText.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }
        } else {
            String responseText = "Method Not Allowed";
            exchange.sendResponseHeaders(405, responseText.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseText.getBytes());
            }
        }
    } 
}




