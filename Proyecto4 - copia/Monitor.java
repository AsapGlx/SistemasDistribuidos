import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class Monitor {

    private static final String LOG_FILE_PATH = "/home/asapglx/Desktop/BD/LOG.txt";
    private static final String SERVER_1_URL = "http://localhost:8080/status"; // URL del primer servidor
    private static final String SERVER_2_URL = "http://localhost:8081/status"; // URL del segundo servidor
    private static final int MONITOR_INTERVAL = 3000; // Intervalo de monitoreo en milisegundos

    public static void main(String[] args) throws IOException {

        clearLogFile();

        Timer timer = new Timer();
        timer.schedule(new MonitorTask(), 0, MONITOR_INTERVAL);

        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
        // Aquí puedes configurar contextos si necesitas manejar solicitudes HTTP
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor de Monitoreo corriendo en puerto 8090.");
    }

    private static void clearLogFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, false))) {
            // Sobrescribir el archivo con una cadena vacía para limpiarlo
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class MonitorTask extends TimerTask {
        public void run() {
            String server1Status = checkServerStatus(SERVER_1_URL);
            String server2Status = checkServerStatus(SERVER_2_URL);
            String logEntry = LocalDateTime.now() + " - Server 1: " + server1Status + ", Server 2: " + server2Status + "\n";
            writeLog(logEntry);
        }

        private String checkServerStatus(String urlString) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int responseCode = conn.getResponseCode();
                return responseCode == 200 ? "Online" : "Offline";
            } catch (Exception e) {
                return "Offline";
            }
        }

        private void writeLog(String logEntry) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
                writer.append(logEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}

