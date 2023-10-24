import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class BlendrApp {
    public static void main(String[] args) throws IOException {
        // Create an HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // Create a context for the root path "/"
        server.createContext("/", new RootHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/signup", new SignupHandler());

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
<<<<<<< HEAD
            String filePath = "/Users/alfonzy/Desktop/GitHub/BeatBlendr/index.html";
=======
            String filePath = "/Users/alfonzy/Desktop/BeatBlendr/index.html";
>>>>>>> parent of 3e59991 (Updated with API)
    
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
            String response = "This is the Login page!";
            sendResponse(exchange, response);
        }
    }
    
    static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "This is the Signup page!";
            sendResponse(exchange, response);
        }
    }


    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

