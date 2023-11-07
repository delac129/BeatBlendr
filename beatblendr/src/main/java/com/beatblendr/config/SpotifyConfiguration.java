package com.beatblendr.config;

import java.net.URI;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;

@Service
public class SpotifyConfiguration {
    @Value("${redirect.server.ip}")
    private String customIp;

    private String clientId = "a5879634c44947c3baaa1a0bb8c87f4d";
    private String clientSecret = "45cb87a03c4f41698b703cf40bac27bc";


     public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }


    public SpotifyApi getSpotifyObject(){
        
        URI redirectURL = SpotifyHttpManager.makeUri(customIp + "/api/get-user-code");

        return new SpotifyApi
                .Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectURL)
                .build();
    }
}
