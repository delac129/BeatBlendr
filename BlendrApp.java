import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        server.createContext("/getTopSongs", new GetTopSongsHandler());


        // Start the server
        server.start();
        System.out.println("Server is running on port 8081...");
    }

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
            String response = "<html><body><h1>Success! You can now use the app.</h1>";
            response += "<a href='/getTopSongs'>Get Top Songs</a></body></html>";
            
            sendResponse(exchange, response);
        }
        
     
    }

    static class GetTopSongsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String accessToken = SpotifyClient.requestAccessTokenUsingClientCredentials();
    
            if (accessToken != null) {
                // Make an API request to Spotify to get your top songs using the access token
                String topSongs = SpotifyClient.getTopSongs(accessToken);
    
                if (topSongs != null) {
                    sendResponse(exchange, "Your Top Played Songs: " + topSongs);
                    return;
                }
            }
    
            // If there's an issue, return an error message
            sendResponse(exchange, "Error retrieving top songs." + accessToken);
        }
    }
    
    
    private static String buildAuthorizationURL(String clientId) {
        String redirectUri = "http://localhost:8081/success";
        String state = generateRandomString(16);
        //String scope = "user-library-read user-top-read playlist-modify-public user-follow-read"; // Add more scopes as needed
        String scope = "user-top-read";
        return "https://accounts.spotify.com/authorize?" +
            "response_type=code" +
            "&client_id=" + clientId +
            "&scope=" + scope +
            "&redirect_uri=" + redirectUri +
            "&state=" + state;
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
     
}

