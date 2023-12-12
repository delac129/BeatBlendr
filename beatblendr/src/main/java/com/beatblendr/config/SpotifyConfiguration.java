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

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;


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
