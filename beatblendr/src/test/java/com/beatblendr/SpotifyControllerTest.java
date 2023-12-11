package com.beatblendr;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.beatblendr.controller.SpotifyController;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
public class SpotifyControllerTest {

    @Autowired
    private SpotifyController spotifyController;

    // Test methods
    @Test
    public void testAggregateTopArtists() throws ParseException, SpotifyWebApiException, IOException {

        String[] result = spotifyController.aggregateTopArtists("abdulrahim24");

        // Assertions
        assertNotNull(result, "Result should not be null");
        assertTrue(result.length > 0, "Result should contain at least one artist URI");
    }


    @Test
    public void testAggregateTopTracks() throws ParseException, SpotifyWebApiException, IOException {

        String[] result = spotifyController.aggregateTopTracks("abdulrahim24");

        // Assertions
        assertNotNull(result, "Result should not be null");
        assertTrue(result.length > 0, "Result should contain at least one track URI");
    }

    @Test
    public void testSelectTracks() throws ParseException, SpotifyWebApiException, IOException {

        List<Track> result = spotifyController.selectTracks("abdulrahim24", 0.55);

        // Assertions
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Result should contain at least one track URI");
    }

    @Test
    public void testChooseGenre() throws ParseException, SpotifyWebApiException, IOException {

        String[] result = spotifyController.chooseGenre("abdulrahim24");

        // Assertions
        assertNotNull(result, "Result should not be null");
        assertTrue(result.length > 0, "Result should contain at least one track URI");
    }

    @Test
    public void testGetRecommendedTracks() throws ParseException, SpotifyWebApiException, IOException {

        TrackSimplified[] result = spotifyController.get_recommended_tracks("abdulrahim24", 0.55, "pop, club");

        // Assertions
        assertNotNull(result, "Result should not be null");
        assertTrue(result.length > 0, "Result should contain at least one track URI");
    }

    @Test
    public void testCreatePlaylistComplex() throws ParseException, SpotifyWebApiException, IOException {

        ResponseEntity<Map<String, Object>> result = spotifyController.createPlaylistComplex("abdulrahim24", "0.55", "pop, club");

        // Assertions
        assertNotNull(result, "Result should not be null");
        
    }

    @Test
    public void testCreatePlaylistSimple() throws ParseException, SpotifyWebApiException, IOException {

        ResponseEntity<Map<String, Object>> result = spotifyController.createPlaylistSimple("abdulrahim24", "0.55");

        // Assertions
        assertNotNull(result, "Result should not be null");
        
    }
}


