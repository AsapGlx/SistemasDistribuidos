import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Cliente {

    private static final String CREDENTIALS_FILE_PATH = "/home/asapglx/Desktop/BD/USUARIOS.txt";
    private static final String STATUS_ENDPOINT = "/status"; // Nuevo endpoint para el estado


    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext(STATUS_ENDPOINT, new StatusHandler());
        server.createContext("/login", new LoginHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor de Login corriendo en puerto 8080.");
    }

    static class StatusHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String responseText = "Server is running";
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, responseText.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseText.getBytes());
                    }
                } else {
                    String responseText = "Method Not Allowed";
                    exchange.sendResponseHeaders(405, responseText.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseText.getBytes());
                    }
                }
            }
        }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseForm(body);

                boolean isAuthenticated = authenticateUser(params.get("username"), params.get("password"));

                String responseText = isAuthenticated ? "Usuario autenticado" : "Credenciales erróneas";
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, responseText.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseText.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String responseText = "Método no permitido";
                exchange.sendResponseHeaders(405, responseText.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseText.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private Map<String, String> parseForm(String formData) {
            Map<String, String> params = new HashMap<>();
            for (String pair : formData.split("&")) {
                int idx = pair.indexOf("=");
                params.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
            return params;
        }

        private boolean authenticateUser(String username, String password) {
            try {
                Map<String, String> credentials = readCredentialsFromFile();
                String storedPassword = credentials.get(username);
                return storedPassword != null && storedPassword.equals(password);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private Map<String, String> readCredentialsFromFile() throws IOException {
            Map<String, String> credentials = new HashMap<>();
            Files.lines(Paths.get(CREDENTIALS_FILE_PATH))
                    .forEach(line -> {
                        String[] parts = line.split(":");
                        credentials.put(parts[0], parts[1]);
                    });
            return credentials;
        }        
    }
}



   
    

