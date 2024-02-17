import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Interfaz {

    private static final Scanner scanner = new Scanner(System.in);
    private static final String LOGIN_SERVER_URL = "http://localhost:8080/login";
    private static final String SUBTITLE_SERVER_URL = "http://localhost:8081/subtitle";
    private static final String MOVIE_SERVER_URL = "http://localhost:8081/movies";
    public int numeroPelicula;

    public static void main(String[] args) {
        if (!authenticate()) {
            System.out.println("Autenticación fallida.");
            return; 
        }

        String movieTitle = selectMovie();
        displaySubtitles(movieTitle);
    }

    private static boolean authenticate() {
        try {
            // Create the URL object for the login server
            URL url = new URL(LOGIN_SERVER_URL);

            // Open a connection to the login server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Enable input and output streams
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Prompt the user to enter their credentials
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            // Set the request body with the entered credentials
            String credentials = "username=" + username + "&password=" + password;
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(credentials.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response code from the server
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine);
            }
            reader.close();

            // Interpretar la respuesta del servidor
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (response.toString().equals("Usuario autenticado")) {
                    System.out.println("Access Granted");
                    return true;
                } else if (response.toString().equals("Credenciales erróneas")) {
                    System.out.println("Invalid Credentials");
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String selectMovie() {
        try {
            // Create the URL object for the subtitle server
            URL url = new URL(MOVIE_SERVER_URL);

            // Open a connection to the subtitle server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response code from the server
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine);
            }
            reader.close();

            // Interpretar la respuesta del servidor
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Películas disponibles:");
                String[] movies = response.toString().split(",");
                for (int i = 0; i < movies.length; i++) {
                    System.out.println((i + 1) + ". " + movies[i]);
                }

                System.out.print("Seleccione una película ingresando el número correspondiente: ");
                int movieNumber = scanner.nextInt();

                if (movieNumber >= 1 && movieNumber <= movies.length) {
                    System.out.println("Seleccionaste la película: " + movies[movieNumber - 1]);
                    return movies[movieNumber - 1];
                } else {
                    System.out.println("Número de película inválido.");
                }
            } else {
                System.out.println("Error al obtener la lista de películas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void displaySubtitles(String movieTitle) {

    int lastDisplayedSubtitleNumber = getLastDisplayedSubtitleNumber(movieTitle); // Implementar este método
    int currentSubtitleNumber = lastDisplayedSubtitleNumber > 0 ? lastDisplayedSubtitleNumber : 1;

        try {
            URL url = new URL(SUBTITLE_SERVER_URL + "?title=" + movieTitle + "&number=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
    
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine).append("\n");
            }
            reader.close();
    
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String[] subtitles = response.toString().split("\n\n");
                long lastStartTime = 0;

                System.out.println("URL solicitada: " + url.toString());
                int responseCode2 = connection.getResponseCode();
                System.out.println("Código de respuesta: " + responseCode2);

                for (String subtitle : subtitles) {
                    String[] subtitleParts = subtitle.split("\n", 3);
                    if (subtitleParts.length >= 2) {
                        String time = subtitleParts[0];
                        String text = subtitleParts[1];
    
                        String[] timeParts = time.split(" --> ");
                        long startTime = convertToMillis(timeParts[0]);
                        long endTime = convertToMillis(timeParts[1]);


                        System.out.println("Respuesta del servidor: " + response.toString());

                        // Esperar hasta el inicio del próximo subtítulo
                        long waitTime = startTime - lastStartTime;
                        if (waitTime > 0) {
                            Thread.sleep(waitTime);
                            System.out.println("Tiempo de inicio: " + startTime + ", Tiempo de espera: " + waitTime);
                        }
    
                        System.out.println(text); // Mostrar el texto del subtítulo
                        lastStartTime = endTime;
                    }
                }
                saveLastDisplayedSubtitleNumber(movieTitle, currentSubtitleNumber);
            } else {
                System.out.println("Error al obtener los subtítulos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void saveLastDisplayedSubtitleNumber(String movieTitle, int currentSubtitleNumber) {
    File file = new File("/home/asapglx/Desktop/BD/last_subtitles.txt"); // Cambia a la ruta de tu archivo
    Map<String, Integer> lastSubtitles = new HashMap<>();

    // Primero, leer el contenido actual del archivo
    try (Scanner scanner = new Scanner(file)) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(":");
            if (parts.length == 2) {
                lastSubtitles.put(parts[0], Integer.parseInt(parts[1]));
            }
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }

    // Actualizar la información para la película actual
    lastSubtitles.put(movieTitle, currentSubtitleNumber);

    // Reescribir el archivo con la información actualizada
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        for (Map.Entry<String, Integer> entry : lastSubtitles.entrySet()) {
            writer.write(entry.getKey() + ":" + entry.getValue());
            writer.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private static int getLastDisplayedSubtitleNumber(String movieTitle) {
    File file = new File("/home/asapglx/Desktop/BD//last_subtitles.txt"); // Cambia a la ruta de tu archivo
    Map<String, Integer> lastSubtitles = new HashMap<>();

    try (Scanner scanner = new Scanner(file)) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(":");
            if (parts.length == 2) {
                lastSubtitles.put(parts[0], Integer.parseInt(parts[1]));
            }
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }

    return lastSubtitles.getOrDefault(movieTitle, 0);
}

    private static long convertToMillis(String time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss,SSS");
        try {
            Date date = format.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
  
