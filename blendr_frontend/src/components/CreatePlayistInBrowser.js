import Header from "./Header";
import { useLocation } from "react-router-dom";
import React, { useEffect, useState } from "react";
import Player from "./Player";
import Playlist from "./Playlist";
import "./Playlist.css";

function CreatePlaylistInBrowser(props) {
  const [userId, setUserId] = useState("");
  const [sdkLoaded, setSdkLoaded] = useState(false);
  const [moodValue, setMoodValue] = useState("");
  const [token, setToken] = useState();
  const [trackPlaying, setTrackPlaying] = useState("");
  const [playlistTracks, setPlaylistTracks] = useState([]);
  let query = new URLSearchParams(useLocation().search);

  useEffect(() => {
    const id = query.get("userId");
    const val = query.get("mood");
    setUserId(id);
    setMood(val);
  }, [query]);

  useEffect(() => {
    if (userId && moodValue) {
      // Check both userId and moodValue
      console.log("Fetching user information " + userId);
      fetch(
        "http://localhost:8080/api/create-playlist?userId=" +
          userId +
          "&mood=" +
          moodValue
      )
        .then((res) => res.json())
        .then((data) => {
          console.log(data);
          console.log(data.accessToken);
          setPlaylistTracks(data.tracks);
          setToken(data.accessToken);
        })
        .catch((error) => {
          console.error("Error fetching the access token:", error);
        });
    }
  }, [userId, moodValue]);

  useEffect(() => {
    if (!sdkLoaded) {
      window.onSpotifyWebPlaybackSDKReady = function () {
        console.log("Spotify SDK is ready");
      };

      const script = document.createElement("script");
      script.src = "https://sdk.scdn.co/spotify-player.js";
      script.async = true;
      script.onload = () => {
        console.log("Spotify SDK Loaded");
        setSdkLoaded(true);
      };
      document.body.appendChild(script);
    }
  }, []);

  function setMood(mood) {
    setMoodValue(mood);
  }

  function updateTrack(track) {
    let currentlyPlaying = trackPlaying;
    if (currentlyPlaying !== track) {
      setTrackPlaying(track);
    }
  }

  function nextTrack() {
    let currentlyPlaying = trackPlaying;
    let indexCurrentlyPlaying = playlistTracks.findIndex((track) => {
      return track.uri === currentlyPlaying;
    });
    let nextTrack = playlistTracks[indexCurrentlyPlaying + 1];
    setTrackPlaying(nextTrack);
  }

  function previousTrack() {
    let currentlyPlaying = trackPlaying;
    let indexCurrentlyPlaying = playlistTracks.findIndex((track) => {
      return track.uri === currentlyPlaying;
    });
    let previousTrack = playlistTracks[indexCurrentlyPlaying - 1];
    setTrackPlaying(previousTrack);
  }

  return (
    <>
      <Header />
      <div>
        <h1 className="playlist-header">
          <b>NAME!</b>
        </h1>
        <div className="row">
          <div className="col">
            {sdkLoaded && token && (
              <Player
                sdkLoaded={sdkLoaded}
                accessToken={token}
                songToPlay={trackPlaying}
                nextSong={nextTrack}
                previousSong={previousTrack}
                tracks={playlistTracks}
              />
            )}
          </div>
          <div className="col">
            <Playlist
              updateTrack={updateTrack}
              songPlaying={trackPlaying}
              tracks={playlistTracks}
            />
          </div>
        </div>
      </div>
    </>
  );
}

export default CreatePlaylistInBrowser;

// <>
//   <Header />
//   {!clicked ? (
//     <div className="moodButtons">
// <button onClick={() => setMood(0.05)}>Very Sad</button>
// <button onClick={() => setMood(0.175)}>Sad</button>
// <button onClick={() => setMood(0.375)}>Somber/Melancholy</button>
// <button onClick={() => setMood(0.625)}>Content/Neutral</button>
// <button onClick={() => setMood(0.825)}>Happy/Excited</button>
// <button onClick={() => setMood(0.95)}>Euphoric/Elated</button>
//     </div>
//   ) : (
//     <div className="playlist-link">
//       {userPlaylistId ? <p>Check Console</p> : <p>Loading playlist...</p>}
//     </div>
//   )}
// </>

//   useEffect(() => {
//     if (token) {
//       loadSpotifySDK();

//       window.onSpotifyWebPlaybackSDKReady = () => {
//         const player = new window.Spotify.Player({
//           name: "Your App Name",
//           getOAuthToken: (callback) => callback(token),
//         });

//         // Error handling
//         player.addListener("initialization_error", ({ message }) => {
//           console.error(message);
//         });
//         player.addListener("authentication_error", ({ message }) => {
//           console.error(message);
//         });
//         player.addListener("account_error", ({ message }) => {
//           console.error(message);
//         });
//         player.addListener("playback_error", ({ message }) => {
//           console.error(message);
//         });

//         // Playback status updates
//         player.addListener("player_state_changed", (state) => {
//           console.log(state);
//         });

//         // Ready
//         player.addListener("ready", ({ device_id }) => {
//           console.log("Ready with Device ID", device_id);
//         });

//         // Connect to the player!
//         player.connect();
//       };
//     }
//   }, [token]);

//   const loadSpotifySDK = () => {
//     const script = document.createElement("script");
//     script.src = "https://sdk.scdn.co/spotify-player.js";
//     script.async = true;
//     document.body.appendChild(script);
//   };
