/* 
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;



import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;


import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;



public class SpotifyClient {
    private static final String CLIENT_ID = "5beff6e7c7564da186c49f289c0d4b86";
    private static final String CLIENT_SECRET = "5bbc482aa0974fd2a62b04f1368e44d6";

    public static String requestAccessTokenUsingClientCredentials() {
        try {
            // Set up the URL for the Spotify token endpoint
            URL url = new URL("https://accounts.spotify.com/api/token");
    
            // Create an HTTP connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
    
            // Set request properties
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic BASE64_ENCODED_CLIENT_CREDENTIALS");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    
            // Build the request body for client credentials flow
            String body = "grant_type=client_credentials";
    
            // Calculate and set content length
            byte[] out = body.getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            connection.setFixedLengthStreamingMode(length);
    
            // Connect to the Spotify token endpoint
            connection.connect();
    
            // Send the request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(out);
            }
    
            // Receive and parse the access token
            InputStream result = connection.getInputStream();
            String responseString = new String(result.readAllBytes());
    
            // Parse the JSON response to get the access token
            int startIndex = responseString.indexOf("access_token") + 15;
            int endIndex = responseString.indexOf("\"", startIndex);
    
            return responseString.substring(startIndex, endIndex);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle this based on your application's requirements
        }
    }

    public static String exchangeAuthorizationCodeForAccessToken(String authorizationCode) {
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

    public static String getTopSongs(String accessToken) {
        try {
            // Set up the URL for the Spotify top tracks endpoint
            URL url = new URL("https://api.spotify.com/v1/me/top/tracks");
    
            // Create an HTTP connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
    
            // Set request properties, including the "Authorization" header
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
    
            // Get the response from the Spotify top tracks endpoint
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
    
                return response.toString();
            } else {
                // Handle the error and log more details
                System.out.println("Error while fetching top songs: " + responseCode);
    
                // Log the response body for further debugging
                BufferedReader errorIn = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorInputLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorInputLine = errorIn.readLine()) != null) {
                    errorResponse.append(errorInputLine);
                }
                System.out.println("Error Response: " + errorResponse.toString());
    
                return null; // Handle this based on your application's requirements
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle this based on your application's requirements
        }
    }
}
*/
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Base64;

public class SpotifyClient {
    private static final String CLIENT_ID = "5beff6e7c7564da186c49f289c0d4b86";
    private static final String CLIENT_SECRET = "5bbc482aa0974fd2a62b04f1368e44d6";

    private static String verifier; // Stores the code verifier

    public static void setVerifier(String verifier) {
        SpotifyClient.verifier = verifier;
    }

    public static String requestAccessTokenUsingClientCredentials() {
        try {
            // Set up the URL for the Spotify token endpoint
            URL url = new URL("https://accounts.spotify.com/api/token");

            // Create an HTTP connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            // Set request properties
            connection.setDoOutput(true);
            String base64Credentials = Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Build the request body for client credentials flow
            String body = "grant_type=client_credentials";

            // Calculate and set content length
            byte[] out = body.getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            connection.setFixedLengthStreamingMode(length);

            // Connect to the Spotify token endpoint
            connection.connect();

            // Send the request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(out);
            }

            // Receive and parse the access token
            InputStream result = connection.getInputStream();
            String responseString = new String(result.readAllBytes());

            // Parse the JSON response to get the access token
            int startIndex = responseString.indexOf("access_token") + 15;
            int endIndex = responseString.indexOf("\"", startIndex);

            return responseString.substring(startIndex, endIndex);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle this based on your application's requirements
        }
    }

    public static String exchangeAuthorizationCodeForAccessToken(String authorizationCode) {
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
            parameters.put("client_id", CLIENT_ID); // Your Spotify client ID
            parameters.put("client_secret", CLIENT_SECRET); // Your Spotify client secret
            parameters.put("code_verifier", verifier); // Set the verifier

            // Write the request body
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }

            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
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

    public static String getTopSongs(String accessToken) {
        try {
            // Set up the URL for the Spotify top tracks endpoint
            URL url = new URL("https://api.spotify.com/v1/me/top/tracks");

            // Create an HTTP connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set request properties, including the "Authorization" header
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Get the response from the Spotify top tracks endpoint
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

                return response.toString();
            } else {
                // Handle the error and log more details
                System.out.println("Error while fetching top songs: " + responseCode);

                // Log the response body for further debugging
                BufferedReader errorIn = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorInputLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorInputLine = errorIn.readLine()) != null) {
                    errorResponse.append(errorInputLine);
                }
                System.out.println("Error Response: " + errorResponse.toString());

                return null; // Handle this based on your application's requirements
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle this based on your application's requirements
        }
    }

    public static void main(String[] args) {
        // Implement your application logic here
    }
}
