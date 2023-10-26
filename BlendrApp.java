import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.LinkedHashMap;



import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;

public class BlendrApp {
    public static void main(String[] args) throws IOException {
        // Create an HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // Create a context for the root path "/"
        server.createContext("/", new RootHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/signup", new SignupHandler());
        server.createContext("/success", new SuccessHandler());

        // Start the server
        server.start();
        System.out.println("Server is running on port 8081...");
    }
/* 
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle incoming HTTP requests
            String filePath = "/Users/alfonzy/Desktop/BeatBlendr/index.html";
            File file = new File(filePath);
            String response = "Hello, World!";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Specify the path to your index.html file
            String filePath = "/Users/alfonzy/Desktop/GitHub/BeatBlendr/index.html";
    
            File file = new File(filePath);
    
            // Check if the file exists
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] content = new byte[(int) file.length()];
                
                // Read the content of the HTML file
                fis.read(content);
                fis.close();
    
                // Set the response content type to HTML
                exchange.getResponseHeaders().set("Content-Type", "text/html");
    
                // Send the response with the HTML content
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                // If the HTML file doesn't exist, send a 404 error response
                String response = "404 - Not Found";
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle the Spotify authorization process and redirect to success page
            String client_id = "5beff6e7c7564da186c49f289c0d4b86";
            String authorizationURL = buildAuthorizationURL(client_id);
            sendRedirectResponse(exchange, authorizationURL);
        }
    }
    
    static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "This is the Signup page!";
            sendResponse(exchange, response);
        }
    }

    static class SuccessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String authorizationCode = exchange.getRequestURI().getQuery().split("=")[1];
            String accessToken = exchangeAuthorizationCodeForAccessToken(authorizationCode);
            if (accessToken != null) {
                String response = "Success! You can now use the app.";
                sendResponse(exchange, response);
            } else {
                String response = "Error during token exchange.";
                sendResponse(exchange, response);
            }
        }
    }
    private static String buildAuthorizationURL(String clientId) {
        String redirectUri = "http://localhost:8081/success";
        String state = generateRandomString(16);
        String scope = "user-read-private user-read-email";

        return "https://accounts.spotify.com/authorize?" +
            "response_type=code" +
            "&client_id=" + clientId +
            "&scope=" + scope +
            "&redirect_uri=" + redirectUri +
            "&state=" + state;
    }

    // Define exchangeAuthorizationCodeForAccessToken without ObjectMapper
    private static String exchangeAuthorizationCodeForAccessToken(String authorizationCode) {
        try {
            // Set up the URL for the Spotify token endpoint
            URL url = new URL("https://accounts.spotify.com/api/token");

            // Create an HTTP connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Set request properties
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Build the request body
            Map<String, String> parameters = new LinkedHashMap<>();
            parameters.put("grant_type", "authorization_code");
            parameters.put("code", authorizationCode);
            parameters.put("redirect_uri", "http://localhost:8081/success"); // Your redirect URI
            parameters.put("client_id", "5beff6e7c7564da186c49f289c0d4b86"); // Your Spotify client ID
            parameters.put("client_secret", "5bbc482aa0974fd2a62b04f1368e44d6"); // Your Spotify client secret

            // Write the request body
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }

            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            OutputStream os = connection.getOutputStream();
            os.write(postDataBytes);

            // Get the response from the Spotify token endpoint
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Success, read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response to get the access token
                String responseString = response.toString();
                int startIndex = responseString.indexOf("access_token") + 15;
                int endIndex = responseString.indexOf("\"", startIndex);

                return responseString.substring(startIndex, endIndex);
            } else {
                // Handle the error, such as by logging or returning an error message
                System.out.println("Error during token exchange: " + responseCode);
                return null; // Handle this based on your application's requirements
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle this based on your application's requirements
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static void sendRedirectResponse(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1); // 302 indicates a temporary redirect
        exchange.close();
    }
    private static String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    static class TopSongsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String accessToken = exchange.getRequestHeaders().getFirst("Authorization");
    
            if (accessToken != null) {
                // Make an API request to Spotify to get your top songs using the access token
                String topSongs = ""; //getTopSongs(accessToken)
    
                if (topSongs != null) {
                    sendResponse(exchange, "Your Top Played Songs: " + topSongs);
                    return;
                }
            }
    
            // If there's an issue, return an error message
            sendResponse(exchange, "Error retrieving top songs.");
        }
    }
    
}

