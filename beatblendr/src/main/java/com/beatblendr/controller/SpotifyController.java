package com.beatblendr.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.beatblendr.config.SpotifyConfiguration;
import com.beatblendr.entity.UserDetails;
import com.beatblendr.entity.UserDetailsRepository;
import com.beatblendr.service.UserProfileService;


import jakarta.servlet.http.HttpServletResponse;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PagingCursorbased;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;
import se.michaelthelin.spotify.requests.data.browse.miscellaneous.GetAvailableGenreSeedsRequest;
import se.michaelthelin.spotify.requests.data.follow.GetUsersFollowedArtistsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import se.michaelthelin.spotify.enums.ModelObjectType;
import com.neovisionaries.i18n.CountryCode;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SpotifyController {

	@Value("${custom.server.ip}")
	private String customIp;
	
	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private SpotifyConfiguration spotifyConfiguration;
	
	@Autowired
	private UserDetailsRepository userDetailsRepository;


    private  Map<String, Track> trackDetailsCache = new HashMap<>();
	
	@GetMapping(value = "login")
	public String spotifyLogin() {
		SpotifyApi object = spotifyConfiguration.getSpotifyObject();
		
		AuthorizationCodeUriRequest authorizationCodeUriRequest = object.authorizationCodeUri()
				.scope("user-library-read user-top-read user-follow-read playlist-modify-private user-modify-playback-state")
				.show_dialog(true)
				.build();
		
		final URI uri = authorizationCodeUriRequest.execute();
		return uri.toString();
	}

    
/*
 * This method is responsible for handling the Spotify user authorization process.
 * It retrieves an authorization code from the incoming request, uses it to obtain
 * access and refresh tokens from Spotify, fetches the user's profile, and then
 * redirects to a specific URL with the user's details or sends an error response
 * if the process fails.
 */
@GetMapping(value = "get-user-code")
public void getSpotifyUserCode(@RequestParam("code") String userCode, HttpServletResponse response) throws IOException {

    // Create a SpotifyApi object from the configuration
    SpotifyApi object = spotifyConfiguration.getSpotifyObject();

    // Build a request to get authorization code using the userCode
    AuthorizationCodeRequest authorizationCodeRequest = object.authorizationCode(userCode).build();
    User user = null;

    try {
        // Execute the request to get authorization code credentials
        final AuthorizationCodeCredentials authorizationCode = authorizationCodeRequest.execute();

        // Set the access and refresh tokens in the SpotifyApi object
        object.setAccessToken(authorizationCode.getAccessToken());
        object.setRefreshToken(authorizationCode.getRefreshToken());

        // Build and execute a request to get the current user's profile
        final GetCurrentUsersProfileRequest getCurrentUsersProfile = object.getCurrentUsersProfile().build();
        user = getCurrentUsersProfile.execute();

        long expiresIn = authorizationCode.getExpiresIn(); // in seconds

        // Convert expiration time to milliseconds and calculate the future expiration time
        long expirationTimeMillis = System.currentTimeMillis() + (expiresIn * 1000);

        // Store the user details along with tokens and expiration time in the database
        userProfileService.insertOrUpdateUserDetails(user, authorizationCode.getAccessToken(), authorizationCode.getRefreshToken(), expirationTimeMillis);

    } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    // Check if the user object is null and handle the response accordingly
    if (user == null) {
        // Send an error response if the user details could not be fetched
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch user details from Spotify");
    } else {
        // Redirect to a specific URL with the user's ID and display name as parameters
        response.sendRedirect(customIp + "/choose-method?id=" + user.getId() + "&username=" + user.getDisplayName());
    }
}

/**
 * This method aggregates the top artists for a given user based on their Spotify listening history
 * and followed artists. It fetches the top artists in short-term, medium-term, and long-term ranges,
 * as well as the artists followed by the user, and compiles a unique list of these artists' URIs.
 */
public String[] aggregateTopArtists(@RequestParam String userId) {
    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }

    // Set the access token for Spotify API calls
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    // Initialize a list to store unique artist URIs
    List<String> topArtistsUriList = new ArrayList<>();

    // Define time ranges for fetching top artists
    String[] timeRanges = {"short_term", "medium_term", "long_term"};
    for(String timeRange : timeRanges) {
        // Build a request to fetch top artists for the specified time range
        GetUsersTopArtistsRequest getUsersTopArtistsRequest = spotifyApi.getUsersTopArtists()
                .time_range(timeRange)
                .limit(50)
                .build();

        try {
            // Execute the request and process the response
            final Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();
            Artist[] artists = artistPaging.getItems();
            for (Artist artist : artists) {
                // Add artist URI to the list if it's not already included
                if (!topArtistsUriList.contains(artist.getUri())) {
                    topArtistsUriList.add(artist.getUri());
                }
            }
        } catch (Exception e) {
            // Log an exception if fetching top artists fails
            System.out.println("Exception occurred while fetching top artists for " + timeRange + ": " + e);
        }
    }

    // Build a request to fetch artists followed by the user
    final GetUsersFollowedArtistsRequest getUsersFollowedArtistsRequest = spotifyApi.getUsersFollowedArtists(ModelObjectType.ARTIST)
            .limit(50)
            .build();

    try {
        // Execute the request and process the response
        final PagingCursorbased<Artist> artistPaging = getUsersFollowedArtistsRequest.execute();
        Artist[] artists = artistPaging.getItems();
        for (Artist artist : artists) {
            // Add artist URI to the list if it's not already included
            if (!topArtistsUriList.contains(artist.getUri())) {
                topArtistsUriList.add(artist.getUri());
            }
        }
    } catch (Exception e) {
        // Log an exception if fetching followed artists fails
        System.out.println("Exception occurred while fetching followed artists for " + userId + ": " + e);
    }

    // Return the list of unique artist URIs
    System.out.println("... Successfully returning artist URI (uniform resource indicator) & User Following list");
    return topArtistsUriList.toArray(new String[0]);
}


/**
 * This method aggregates the top tracks for a given user based on their top artists on Spotify.
 * It fetches the top tracks of each artist in the user's top artist list and compiles a unique
 * list of these tracks' URIs.
 */
public String[] aggregateTopTracks(@RequestParam String userId) {
    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Define the country code for the top tracks request
    final CountryCode countryCode = CountryCode.SE;

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }

    // Set the access token for Spotify API calls
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    // Aggregate top artists for the user
    String[] topArtists = aggregateTopArtists(userId);

    // Initialize a list to store unique track URIs
    List<String> topTracksUriList = new ArrayList<>();

    // Iterate through each artist and fetch their top tracks
    for(String artist : topArtists){
        System.out.println("... Getting Top Tracks");
        String[] parts = artist.split(":");
        String id = parts[2];

        // Build a request to fetch top tracks of the artist
        final GetArtistsTopTracksRequest getArtistsTopTracksRequest = spotifyApi.getArtistsTopTracks(id, countryCode)
                .build();

        try{
            // Execute the request and process the response
            final Track[] tracks = getArtistsTopTracksRequest.execute();
            for (Track track : tracks) {
                // Add track URI to the list if it's not already included
                topTracksUriList.add(track.getUri());
            }

        } catch (Exception e) {
            // Log an exception if fetching top tracks fails
            System.out.println("Exception occurred while fetching top tracks for user: " + userId + ": " + e);
        }
    }

    // Return the list of unique track URIs
    System.out.println("... Successfully returning Top Tracks URI (uniform resource indicator)");

    return topTracksUriList.toArray(new String[0]);
}


/**
 * This method selects tracks for a given user based on their mood score.
 * It aggregates the top tracks for the user, retrieves their audio features,
 * and filters them according to the mood score to create a curated list of tracks.
 */
public List<Track> selectTracks(@RequestParam String userId, double mood) {
    System.out.println("...selecting tracks");

    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    // Initialize a list to store selected tracks
    List<Track> selectedTracks = new ArrayList<>();

    // Convert array of top tracks' URIs to a modifiable ArrayList
    ArrayList<String> trackListWithUri = new ArrayList<>(Arrays.asList(aggregateTopTracks(userId)));
    ArrayList<String> trackList = new ArrayList<>();

    // Extract track IDs from URIs
    for (String uri : trackListWithUri) {
        String[] parts = uri.split(":");
        String trackId = parts[2];
        trackList.add(trackId);
    }

    // Shuffle the list of top tracks
    Collections.shuffle(trackList);

    // Divide the list into smaller groups for batch processing
    List<List<String>> groupedTracks = new ArrayList<>();
    final int maxGroupSize = 50;
    for (int i = 0; i < trackList.size(); i += maxGroupSize) {
        groupedTracks.add(new ArrayList<>(trackList.subList(i, Math.min(i + maxGroupSize, trackList.size()))));
    }

    // Process each group of tracks
    for (List<String> tracks : groupedTracks) {
        try {
            // Retrieve audio features for each track
            AudioFeatures[] tracksAllData = spotifyApi.getAudioFeaturesForSeveralTracks(tracks.toArray(new String[0])).build().execute();

            // Identify tracks that need to be fetched from Spotify
            List<String> tracksToFetch = tracks.stream().filter(id -> !trackDetailsCache.containsKey(id)).collect(Collectors.toList());

            // Fetch and cache track details if not already in cache
            if (!tracksToFetch.isEmpty()) {
                Track[] fetchedTrackDetails = spotifyApi.getSeveralTracks(tracksToFetch.toArray(new String[0])).build().execute();
                for (Track trackDetail : fetchedTrackDetails) {
                    trackDetailsCache.put(trackDetail.getId(), trackDetail);
                }
            }

            // Process each track based on its audio features
            for (int i = 0; i < tracksAllData.length; i++) {
                AudioFeatures trackData = tracksAllData[i];
                Track trackDetail = trackDetailsCache.get(tracks.get(i));

                 if (trackData != null) {
                    double valence = trackData.getValence();
                    double danceability = trackData.getDanceability();
                    double energy = trackData.getEnergy();
                    
                    if (mood < 0.10) {
						if (valence <= (mood + 0.15) && danceability <= (mood * 8) && energy <= (mood * 10)) {
							selectedTracks.add(trackDetail);
						}
					} else if (0.10 <= mood && mood < 0.25) {
						if ((mood - 0.075) <= valence && valence <= (mood + 0.075) && danceability <= (mood * 4) && energy <= (mood * 5)) {
							selectedTracks.add(trackDetail);
						}
					} else if (0.25 <= mood && mood < 0.50) {
						if ((mood - 0.05) <= valence && valence <= (mood + 0.05) && danceability <= (mood * 1.75) && energy <= (mood * 1.75)) {
							selectedTracks.add(trackDetail);
						}
					} else if (0.50 <= mood && mood < 0.75) {
						if ((mood - 0.075) <= valence && valence <= (mood + 0.075) && danceability >= (mood / 2.5) && energy >= (mood / 2)) {
							selectedTracks.add(trackDetail);
						}
					} else if (0.75 <= mood && mood < 0.90) {
						if ((mood - 0.075) <= valence && valence <= (mood + 0.075) && danceability >= (mood / 2) && energy >= (mood / 1.75)) {
							selectedTracks.add(trackDetail);
						}
					} else if (mood >= 0.90) {
						if ((mood - 0.15) <= valence && valence <= 1 && danceability >= (mood / 1.75) && energy >= (mood / 1.5)) {
							selectedTracks.add(trackDetail);
						}
					}
                }
            }
        } catch (Exception e) {
            // Log an exception if fetching track data fails
            System.out.println("Error occurred while fetching track data: " + e.getMessage());
        }
    }
    
    System.out.println("... Successfully returning selected tracks");
    return selectedTracks;
}



/**
 * This method retrieves an array of available genre seeds from Spotify.
 * These genres can be used for various features like generating playlists
 * or recommendations based on genre preferences.
 */
@GetMapping(value = "choose-genre")
public String[] chooseGenre(@RequestParam String userId) {

    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if (newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }

    // Set the access token for Spotify API calls
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    // Build a request to get available genre seeds from Spotify
    GetAvailableGenreSeedsRequest getAvailableGenreSeedsRequest = spotifyApi.getAvailableGenreSeeds().build();

    try {
        // Execute the request and return the genre seeds
        final String[] genreSeeds = getAvailableGenreSeedsRequest.execute();
        System.out.println(".... Successfully returned genre seeds");
        return genreSeeds;

    } catch (Exception e) {
        // Log an exception if fetching genre seeds fails
        System.out.println("Error occurred while fetching track data: " + e.getMessage());
    }

    // Return an empty array if the try block fails
    return new String[0];
}


/**
 * This method provides personalized track recommendations for a user based on their top artists and specified genre.
 * It fetches the top artists for the user, uses these artists as seeds for the Spotify recommendation algorithm,
 * and returns an array of recommended tracks. If the top artists are insufficient or the recommendation process fails,
 * it returns an empty array.
 */
public TrackSimplified[] get_recommended_tracks(@RequestParam String userId, @RequestParam double mood, @RequestParam String genre) {
    // Initialize an array to hold the default empty track list
    TrackSimplified[] defaultTracks = new TrackSimplified[0];

    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }

    // Set the access token for Spotify API calls
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    // Initialize an array to store artist IDs
    String[] artists = new String[2];  

    // Aggregate top artists for the user
    String[] topArtists = aggregateTopArtists(userId);

    // Check if there are enough top artists to proceed
    if (topArtists == null || topArtists.length < 2) {
        // Return default empty array if topArtists is null or has less than 2 elements
        return defaultTracks;
    }

    // Extract artist IDs from the top two artists
    for (int i = 0; i < 2; i++) {
        String[] parts = topArtists[i].split(":");
        String artistId = parts[2];
        artists[i] = artistId;
    }
    String topArtist = artists[0] + "," + artists[1];

    // Build a request for track recommendations based on the top artists and genre
    GetRecommendationsRequest getRecommendationsRequest = spotifyApi.getRecommendations()
        .limit(50)
        .seed_artists(topArtist)
        .seed_genres(genre)
        .build();

    try {
        // Execute the request and retrieve recommendations
        final Recommendations recommendations = getRecommendationsRequest.execute();
        TrackSimplified[] tracks = recommendations.getTracks();

        // Check if any tracks were received
        if (tracks == null) {
            System.out.println("No tracks received from Spotify API.");
            return defaultTracks;
        }
        return tracks;
    } catch(Exception e) {
        // Log an exception if the recommendation process fails
        System.out.println("Error occurred while fetching track data: " + e.getMessage());
        return defaultTracks;
    }
}


/**
 * This method creates a playlist on Spotify based on the user's mood and preferred genre.
 * It generates a playlist with recommended tracks, adds them to the playlist, and returns
 * the playlist details, including the tracks, access token, and playlist ID, to the front end.
 */
@GetMapping(value = "create-playlist-complex")
public ResponseEntity<Map<String, Object>> createPlaylistComplex(@RequestParam String userId, @RequestParam String mood, @RequestParam String genre) {
    System.out.println("... creating playlist complex");
    System.out.println("Received userId: " + userId);
    System.out.println("Received moodString: " + mood);

    // Initialize a response map to hold the playlist details
    Map<String, Object> response = new HashMap<>();

    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }

    // Set the access token for Spotify API calls
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    // Parse and validate the mood value
    double mood_double = 50.0; // Default value
    if (mood != null) {
        try {
            mood_double = Double.parseDouble(mood);
        } catch (NumberFormatException e) {
            System.out.println("Invalid mood value: " + mood);
        } catch (NullPointerException e) {
            System.out.println("Mood string is null.");
        }
    } else {
        System.out.println("Mood string is missing.");
    }

    // Proceed if the mood value is valid
    if(mood_double != 50.0){
        // Get recommended tracks based on mood and genre
        TrackSimplified[] selectedTracks = get_recommended_tracks(userId, mood_double, genre);

        try {
            // Create a new playlist for the user
            String playlistName = "Blendr Beta " + mood;
            CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                .public_(false)
                .build();
            Playlist playlist = createPlaylistRequest.execute();
            String playlistId = playlist.getId();
            response.put("playlistId", playlistId);

            // Convert the TrackSimplified objects to their URI and shuffle the list
            List<String> selectedTracksUri = Arrays.stream(selectedTracks)
                .map(TrackSimplified::getUri)
                .collect(Collectors.toList());
            Collections.shuffle(selectedTracksUri);

            // Limit the number of tracks to add to 30
            List<String> tracksToAddUris = selectedTracksUri.stream()
                .limit(30)
                .collect(Collectors.toList());

            // Add tracks to the playlist
            if (!tracksToAddUris.isEmpty()) {
                AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
                    .addItemsToPlaylist(playlistId, tracksToAddUris.toArray(new String[0]))
                    .build();
                addItemsToPlaylistRequest.execute();
            }

            // Add tracks to the response
            response.put("tracks", selectedTracks);

            System.out.println(".... Playlist id = " + playlistId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log an exception if creating a playlist fails
            System.out.println("Error occurred while creating playlist: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    } else {
        // Return a bad request response if the mood value is invalid
        return ResponseEntity.badRequest().body(null);
    }
}




/**
 * This method creates a simple playlist on Spotify based on the user's mood.
 * It generates a playlist with tracks selected based on the mood, adds them to the playlist, 
 * and returns the playlist details, including the tracks, access token, and playlist ID, to the front end.
 */
@GetMapping(value = "create-playlist-simple")
public ResponseEntity<Map<String, Object>> createPlaylistSimple(@RequestParam String userId, @RequestParam String mood) {

    // Initialize a response map to hold the playlist details
    Map<String, Object> response = new HashMap<>();

    // Fetch user details based on the provided userId
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);

    // Create a SpotifyApi object from configuration
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    // Refresh access token if needed and update userDetails
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }

    // Set the access token for Spotify API calls and add it to the response
    spotifyApi.setAccessToken(userDetails.getAccessToken());
    response.put("accessToken", userDetails.getAccessToken());

    // Parse and validate the mood value
    double mood_double = 50.0; // Default value
    if (mood != null) {
        try {
            mood_double = Double.parseDouble(mood);
        } catch (NumberFormatException e) {
            System.out.println("Invalid mood value: " + mood);
        } catch (NullPointerException e) {
            System.out.println("Mood string is null.");
        }
    } else {
        System.out.println("Mood string is missing.");
    }

    // Proceed if the mood value is valid
    if(mood_double != 50.0){
        // Select tracks based on mood and add the selected genre to the response
        List<Track> selectedTracks = selectTracks(userId, mood_double);
        response.put("genres", chooseGenre(userId));

        // Extract URIs from the selected tracks
        List<String> selectedTracksUri = selectedTracks.stream()
            .map(Track::getUri)
            .collect(Collectors.toList());

        try {
            // Create a new playlist for the user
            String playlistName = "Blendr " + mood;
            CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                .public_(false)
                .build();
            Playlist playlist = createPlaylistRequest.execute();
            String playlistId = playlist.getId();
            response.put("playlistId", playlistId);

            // Shuffle the track URIs and limit to 30 tracks
            Collections.shuffle(selectedTracksUri);
            List<Track> tracksToAdd = selectedTracksUri.stream()
                .limit(30)
                .map(uri -> selectedTracks.stream().filter(track -> track.getUri().equals(uri)).findFirst().orElse(null))
                .collect(Collectors.toList());

            // Extract URIs of tracks to add and add them to the playlist
            List<String> tracksToAddUri = tracksToAdd.stream()
                .map(Track::getUri)
                .collect(Collectors.toList());
            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, tracksToAddUri.toArray(new String[0])).build();
            addItemsToPlaylistRequest.execute();

            // Add the list of Track objects to the response
            response.put("tracks", tracksToAdd);

            System.out.println(".... Playlist id = " + playlistId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log an exception if creating a playlist fails
            System.out.println("Error occurred while creating playlist: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    } else {
        // Return a bad request response if the mood value is invalid
        return ResponseEntity.badRequest().body(null);
    }
}



    /**
     * Refreshes the Spotify access token using the given refresh token.
     * This function should be called every time we want to access a user's information
     * and includes the logic for refreshing the access token if needed.
     *
     * The refresh token used to obtain a new access token.
     * return The new access token, or null if the refresh process fails.
     */
    private String refreshAccessToken(String refreshToken) {
        // Build a new SpotifyApi instance with client credentials and the refresh token
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(spotifyConfiguration.getClientId())
            .setClientSecret(spotifyConfiguration.getClientSecret())
            .setRefreshToken(refreshToken)
            .build();

        try {
            // Attempt to refresh the access token
            AuthorizationCodeCredentials credentials = spotifyApi.authorizationCodeRefresh().build().execute();
            // Return the new access token
            return credentials.getAccessToken();
        } catch (SpotifyWebApiException | IOException e) {
            System.out.println("Error refreshing access token: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Refreshes the access token for a user if it is expired.
     *
     * The UserDetails object containing the user's token information.
     * return The refreshed access token, or null if no refresh is needed or if the process fails.
     */
    public String refreshAccessTokenIfNeeded(UserDetails userDetails) {
        try {
            // Check if the current token is expired
            if (isTokenExpired(userDetails)) {
                // Refresh and return the new access token
                return refreshAccessToken(userDetails.getRefreshToken());
            }
        } catch (Exception e) {
            System.out.println("Error in refreshing access token if needed: " + e.getMessage());
        }
        // Return null if the token is not expired or in case of an error
        return null;
    }

    /**
     * Checks if the current access token for a user is expired.
     *
     *  The UserDetails object containing the user's token information.
     * return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(UserDetails userDetails) {
        // Get the current system time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        // Get the expiration time of the token from UserDetails
        Long expiration = userDetails.getExpirationTimeMillis();
        // Return true if the current time is greater than the expiration time, indicating the token is expired
        return expiration == null || currentTimeMillis > expiration;
    }

}
