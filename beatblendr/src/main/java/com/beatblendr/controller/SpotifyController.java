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
import org.springframework.http.HttpStatus;
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
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Array;

import jakarta.servlet.http.HttpServletResponse;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PagingCursorbased;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import se.michaelthelin.spotify.requests.data.follow.GetUsersFollowedArtistsRequest;
import se.michaelthelin.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
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

	@GetMapping(value = "get-user-code")
	public void getSpotifyUserCode(@RequestParam("code") String userCode, HttpServletResponse response)	throws IOException {
		SpotifyApi object = spotifyConfiguration.getSpotifyObject();
		
		AuthorizationCodeRequest authorizationCodeRequest = object.authorizationCode(userCode).build();
		User user = null;
		
		try {
			// when you initially retrieve the access token
			final AuthorizationCodeCredentials authorizationCode = authorizationCodeRequest.execute();

			object.setAccessToken(authorizationCode.getAccessToken());
			object.setRefreshToken(authorizationCode.getRefreshToken());
			
			final GetCurrentUsersProfileRequest getCurrentUsersProfile = object.getCurrentUsersProfile().build();
			user = getCurrentUsersProfile.execute();

			
			
			long expiresIn = authorizationCode.getExpiresIn(); // This is typically in seconds

			// Calculate the expiration time
			long expirationTimeMillis = System.currentTimeMillis() + (expiresIn * 1000); // Convert to milliseconds

			// Store this expirationTimeMillis in the database alongside the token
			userProfileService.insertOrUpdateUserDetails(user, authorizationCode.getAccessToken(), authorizationCode.getRefreshToken(), expirationTimeMillis);

		} catch (SpotifyWebApiException e) {
			System.out.println("SpotifyWebApi exception starts here");
            e.printStackTrace();
			System.out.println("SpotifyWebApi exception ends here");
        } catch (IOException e) {
			System.out.println("IO exception starts here");
            e.printStackTrace();
			System.out.println("IO exception ends here");
        } catch (Exception e) {
			System.out.println("General exception starts here");
            e.printStackTrace();
			System.out.println("General exception ends here");
        }

        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch user details from Spotify");
            return;
        }else{
            response.sendRedirect(customIp + "/create-playlist-method?id="+user.getId()+"&username="+user.getDisplayName());
        }
	}
	
// private String getAccessToken(@RequestParam String userId) {
//     UserDetails userDetails = userDetailsRepository.findByRefId(userId);

//     String accessToken;
//     try {
//         accessToken = userDetails.getAccessToken();
//         if (accessToken == null) {
//             return null;
//         }
//     } catch (Exception e) {
//         System.out.println("Exception occurred while getting the access token: " + e);
//         return null;
//     }
//     return null;
// }
	
	@GetMapping(value = "user-saved-album")
	public SavedAlbum[] getCurrentUserSavedAlbum(@RequestParam String userId) {
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);
    SpotifyApi object = spotifyConfiguration.getSpotifyObject();


	String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if (newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
        // Update userDetails in your database if necessary
    }
    
    object.setAccessToken(userDetails.getAccessToken());
		
		final GetCurrentUsersSavedAlbumsRequest getUsersTopArtistsRequest = object.getCurrentUsersSavedAlbums()
				.limit(50)
				.offset(0)
				.build();

		try {
			final Paging<SavedAlbum> artistPaging = getUsersTopArtistsRequest.execute();

			return artistPaging.getItems();
		} catch (Exception e) {
			System.out.println("Exception occurred while fetching user saved album: " + e);
		}
		
		return new SavedAlbum[0];
	}

@GetMapping(value = "user-top-songs")
public Track[] getUserTopTracks(@RequestParam String userId) {
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);
    SpotifyApi object = spotifyConfiguration.getSpotifyObject();
    
    // Check and refresh the access token if necessary
    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if (newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
        // Update userDetails in your database if necessary
    }
    
    object.setAccessToken(userDetails.getAccessToken());
    
    final GetUsersTopTracksRequest getUsersTopTracksRequest = object.getUsersTopTracks()
            .time_range("medium_term")
            .limit(10)
            .offset(0)
            .build();

    try {
        final Paging<Track> trackPaging = getUsersTopTracksRequest.execute();
        return trackPaging.getItems();
    } catch (Exception e) {
        System.out.println("Exception occurred while fetching top songs: " + e);
    }
    
    return new Track[0];
}

@GetMapping(value = "aggregate-top-artists")
private String[] aggregateTopArtists(@RequestParam String userId) {
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();
    final ModelObjectType type = ModelObjectType.ARTIST;

    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    List<String> topArtistsUriList = new ArrayList<>();

    String[] timeRanges = {"short_term", "medium_term", "long_term"};
    for(String timeRange : timeRanges) {
        GetUsersTopArtistsRequest getUsersTopArtistsRequest = spotifyApi.getUsersTopArtists()
                .time_range(timeRange)
                .limit(50)
                .build();

        try {
			System.out.println("... Getting Top Tracks");
            final Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();
            Artist[] artists = artistPaging.getItems();
            for (Artist artist : artists) {
                // Check if the artist URI is not already in the list
                if (!topArtistsUriList.contains(artist.getUri())) {
                    topArtistsUriList.add(artist.getUri());
                }
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while fetching top artists for " + timeRange + ": " + e);
        }
    }

    final GetUsersFollowedArtistsRequest getUsersFollowedArtistsRequest = spotifyApi.getUsersFollowedArtists(type)
            //.after("0LcJLqbBmaGUft1e9Mm8HV")
            .limit(50)
            .build();

    try {
		System.out.println("... Getting User Following List");
        final PagingCursorbased<Artist> artistPaging = getUsersFollowedArtistsRequest.execute();
        Artist[] artists = artistPaging.getItems();
        for (Artist artist : artists) {
            // Check if the artist URI is not already in the list
            if (!topArtistsUriList.contains(artist.getUri())) {
                topArtistsUriList.add(artist.getUri());
            }
        }
    } catch (Exception e) {
        System.out.println("Exception occurred while fetching followed artists for " + userId + ": " + e);
    }
	System.out.println("... Successfully returning artist URI (uniform resource indicator) & User Following list");
    return topArtistsUriList.toArray(new String[0]);
}



@GetMapping(value = "aggregate-top-tracks")
private String[] aggregateTopTracks(@RequestParam String userId) {
	UserDetails userDetails = userDetailsRepository.findByRefId(userId);
	SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();
	final CountryCode countryCode = CountryCode.SE;

	String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }
    spotifyApi.setAccessToken(userDetails.getAccessToken());

	String[] topArtists = aggregateTopArtists(userId);
	List<String> topTracksUriList = new ArrayList<>();

	for(String artist : topArtists){
		System.out.println("... Getting Top Tracks");
		String[] parts = artist.split(":");
        String id = parts[2];
		final GetArtistsTopTracksRequest getArtistsTopTracksRequest = spotifyApi.getArtistsTopTracks(id, countryCode)
				.build();
		try{
			final Track[] tracks = getArtistsTopTracksRequest.execute();
			for (Track track : tracks) {
				topTracksUriList.add(track.getUri());
			}

		}catch (Exception e) {
        System.out.println("Exception occurred while fetching top tracks for user: " + userId + ": " + e);
    }
	}

	System.out.println("... Successfully returning Top Tracks URI (uniform resource indicator) ");


	return topTracksUriList.toArray(new String[0]);
}

// This function returns the 30 tracks from the selected mood. 
public List<Track> selectTracks(@RequestParam String userId, double mood) {
    System.out.println("...selecting tracks");
    UserDetails userDetails = userDetailsRepository.findByRefId(userId);
    SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

    String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }
    spotifyApi.setAccessToken(userDetails.getAccessToken());


    List<Track> selectedTracks = new ArrayList<>();

    // Convert array to a modifiable ArrayList, becuase using collections shuffle requires a list. 
    ArrayList<String> trackListWithUri = new ArrayList<>(Arrays.asList(aggregateTopTracks(userId)));
    ArrayList<String> trackList = new ArrayList<>();


    // Split the URI by colon and take the third part, which is the track ID
	for (String uri : trackListWithUri) {
        String[] parts = uri.split(":");
        String trackId = parts[2];
        trackList.add(trackId);
    }

    // Shuffle the top tracks
    Collections.shuffle(trackList);

    // Divide the list into groups of 50 or less
    List<List<String>> groupedTracks = new ArrayList<>();
    final int maxGroupSize = 50;
    for (int i = 0; i < trackList.size(); i += maxGroupSize) {
        groupedTracks.add(new ArrayList<>(trackList.subList(i, Math.min(i + maxGroupSize, trackList.size()))));
    }

    for (List<String> tracks : groupedTracks) {
        try {
            
            AudioFeatures[] tracksAllData = spotifyApi.getAudioFeaturesForSeveralTracks(tracks.toArray(new String[0])).build().execute();

            // Only fetch tracks which are not in cache.
            List<String> tracksToFetch = tracks.stream().filter(id -> !trackDetailsCache.containsKey(id)).collect(Collectors.toList());

            if (!tracksToFetch.isEmpty()) {
                Track[] fetchedTrackDetails = spotifyApi.getSeveralTracks(tracksToFetch.toArray(new String[0])).build().execute();

                for (Track trackDetail : fetchedTrackDetails) {
                    trackDetailsCache.put(trackDetail.getId(), trackDetail);
                }
            }

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
            System.out.println("Error occurred while fetching track data: " + e.getMessage());
        }
    }
	
	System.out.println("... Successfully returning selected tracks ");
    return selectedTracks;
}


// This function returns an 3 values to the front end. The Tracks object, accessToken, and the playlist id.

@GetMapping(value = "create-playlist")
public ResponseEntity<Map<String, Object>> createPlaylist(@RequestParam String userId, @RequestParam(required = false) String mood) {
    System.out.println("... creating playlist");
    System.out.println("Received userId: " + userId);
    System.out.println("Received moodString: " + mood);

    Map<String, Object> response = new HashMap<>();

	UserDetails userDetails = userDetailsRepository.findByRefId(userId);
	SpotifyApi spotifyApi = spotifyConfiguration.getSpotifyObject();

	String newAccessToken = refreshAccessTokenIfNeeded(userDetails);
    if(newAccessToken != null) {
        userDetails.setAccessToken(newAccessToken);
    }
    spotifyApi.setAccessToken(userDetails.getAccessToken());

    response.put("accessToken", userDetails.getAccessToken());
    // Default value
    double mood_double = 50.0;
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


    if(mood_double != 50.0){
        List<Track> selectedTracks = selectTracks(userId, mood_double);

        // Extract URIs from the selected tracks for adding them to Spotify playlist
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

        // After shuffling the URIs
        Collections.shuffle(selectedTracksUri);

        List<Track> tracksToAdd = selectedTracksUri.stream()
            .limit(30)
            .map(uri -> selectedTracks.stream().filter(track -> track.getUri().equals(uri)).findFirst().orElse(null))
            .collect(Collectors.toList());


        // Get the URIs from the first 30 tracks from the shuffled list
        List<String> tracksToAddUri = tracksToAdd.stream()
            .map(Track::getUri)
            .collect(Collectors.toList());
        AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, tracksToAddUri.toArray(new String[0])).build();
        addItemsToPlaylistRequest.execute();

        // Add tracks to response map (This time, it's the list of Track objects)
        response.put("tracks", tracksToAdd);

        System.out.println(".... Playlist id = " + playlistId);
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        System.out.println("Error occurred while creating playlist: " + e.getMessage());
        return ResponseEntity.badRequest().body(null);
    }
    }
    else{
       return ResponseEntity.badRequest().body(null);
    }
}


// this function should be called everytime we want to access a users info
// includes the logic from isExpired and refresh token to handle refreshing if needed.
private String refreshAccessToken(String refreshToken) {
    SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setClientId(spotifyConfiguration.getClientId())
        .setClientSecret(spotifyConfiguration.getClientSecret())
        .setRefreshToken(refreshToken)
        .build();

    try {
        AuthorizationCodeCredentials credentials = spotifyApi.authorizationCodeRefresh().build().execute();
        return credentials.getAccessToken();
    } catch (SpotifyWebApiException | IOException e) {
    System.out.println("Error refreshing access token: " + e.getMessage());
    return null;
	} catch (Exception e) {
		System.out.println("Unexpected error: " + e.getMessage());
		return null;
	}
}

//function to call isExpired, if it is then return the refresh token 
private String refreshAccessTokenIfNeeded(UserDetails userDetails) {
    try {
        // check token expired
        if (isTokenExpired(userDetails)) {
            return refreshAccessToken(userDetails.getRefreshToken());
        }
    } catch (Exception e) {
        System.out.println("Error in refreshing access token if needed: " + e.getMessage());
    }
    return null;
}


//check if token expired
private boolean isTokenExpired(UserDetails userDetails) {
    //current time
    long currentTimeMillis = System.currentTimeMillis();
    // expiration time
	Long expiration = userDetails.getExpirationTimeMillis();
    //if currentTime is greater than experied, then return true
    return expiration == null || currentTimeMillis > expiration;
}
}
