import React, { useState, useEffect } from "react";
import "./Playlist.css";

let player; // Make player a module-scoped variable

const Player = (props) => {
  const [currentTrack, setCurrentTrack] = useState(null);
  const [isPlayerReady, setIsPlayerReady] = useState(false);

  console.log(props.sdkLoaded);
  console.log(props.accessToken);

  const play = ({
    spotify_uri,
    playerInstance: {
      _options: { id },
    },
  }) => {
    fetch(`https://api.spotify.com/v1/me/player/play?device_id=${id}`, {
      method: "PUT",
      body: JSON.stringify({ uris: [spotify_uri] }),
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${props.accessToken}`,
      },
    });
    console.log("Fetch to sdk made");
  };

  useEffect(() => {
    if (props.accessToken && props.sdkLoaded) {
      if (!player) {
        player = new window.Spotify.Player({
          name: "BeatBlendr Project",
          getOAuthToken: (callback) => {
            callback(props.accessToken);
          },
          volume: 0.5,
        });

        player.addListener("not_ready", ({ device_id }) => {
          console.log("Device ID is not ready for playback", device_id);
        });

        player
          .connect()
          .then((success) => {
            if (success) {
              console.log(
                "The Web Playback SDK successfully connected to Spotify!"
              );
              setIsPlayerReady(true);
            } else {
              console.warn("Failed to connect the Web Playback SDK to Spotify");
            }
          })
          .catch((error) => {
            console.error(
              "Error connecting the Web Playback SDK to Spotify:",
              error
            );
          });

        player.on("ready", async ({ device_id }) => {
          console.log("Player with device ID is ready:", device_id);
          setIsPlayerReady(true);
        });

        player.addListener("player_state_changed", (track) => {
          setCurrentTrack(track);
          console.log(track);
        });
      }
    }
  }, [props.accessToken, props.sdkLoaded]);

  useEffect(() => {
    const handlePlayerStateChanged = (track) => {
      setCurrentTrack(track);
    };

    if (player) {
      player.addListener("player_state_changed", handlePlayerStateChanged);
    }

    return () => {
      if (player) {
        player.removeListener("player_state_changed", handlePlayerStateChanged);
      }
    };
  }, [props.accessToken]);

  useEffect(() => {
    if (player && props.songToPlay) {
      play({
        playerInstance: player,
        spotify_uri: props.songToPlay,
      });
    }
  }, [props.songToPlay]);

  const togglePlay = () => {
    if (isPlayerReady && player && player.togglePlay) {
      player.togglePlay();
    } else {
      console.warn("Player is not ready or togglePlay method is missing");
    }
  };

  const goToNextSong = () => {
    props.nextSong();
  };

  const goToPreviousSong = () => {
    props.previousSong();
  };

  let trackName = currentTrack
    ? currentTrack.track_window.current_track.name
    : "";

  let artistName = currentTrack
    ? currentTrack.track_window.current_track.artists
        .map((artist) => artist.name)
        .join(", ")
    : "";
  let albumArt = currentTrack
    ? currentTrack.track_window.current_track.album.images[0].url
    : "../static/images/turntable.jpeg";

  return (
    <div>
      <img
        className="album"
        src={albumArt}
        height="400"
        width="460"
        alt="Album Art"
      ></img>
      <button id="button_bw" className="btn" onClick={goToPreviousSong}>
        <i className="fa fa-backward"></i>
      </button>
      <button id="button_play" className="btn" onClick={togglePlay}>
        <i className="fa fa-play"></i>
      </button>
      <button id="button_fw" className="btn" onClick={goToNextSong}>
        <i className="fa fa-forward"></i>
      </button>
      <p className="track"> Track: {trackName} </p>
      <p className="artists"> Artist(s): {artistName} </p>
    </div>
  );
};

export default Player;

// const { Spotify } = window;
// import React, { useState, useEffect } from "react";
// import "./Playlist.css";

// const Player = (props) => {
//   const [currentTrack, setCurrentTrack] = useState(null);

//   useEffect(() => {
//     window.onSpotifyWebPlaybackSDKReady = () => {
//       const player = new Spotify.Player({
//         name: "BeatBlendr Project",
//         getOAuthprops.accessToken: (callback) => {
//           callback(props.accessprops.accessToken);
//         },
//         volume: 0.5,
//       });

//     player.addListener("not_ready", ({ device_id }) => {
//       console.log("Device ID is not ready for playback", device_id);
//     });

//     player.connect().then((success) => {
//       if (success) {
//         console.log(
//           "The Web Playback SDK successfully connected to Spotify!"
//         );
//       }
//     });

//     player.on("ready", async (data) => {
//       console.log("Let the music play on!");
//     });

//     player.addListener("player_state_changed", (track) => {
//       setCurrentTrack(track);
//       console.log(track);
//     });
//   };

//   return () => {
//     // Optional cleanup function to disconnect player if needed.
//     // player.disconnect();
//   };
// }, [props.accessprops.accessToken]);

//   useEffect(() => {
//     play({
//       playerInstance: player,
//       spotify_uri: props.songToPlay,
//     });
//   }, [props.songToPlay]);

//   const play = ({
//     spotify_uri,
//     playerInstance: {
//       _options: { id },
//     },
//   }) => {
//     fetch(`https://api.spotify.com/v1/me/player/play?device_id=${id}`, {
//       method: "PUT",
//       body: JSON.stringify({ uris: [spotify_uri] }),
//       headers: {
//         "Content-Type": "application/json",
//         Authorization: `Bearer ${props.accessprops.accessToken}`,
//       },
//     });
//   };

//   const togglePlay = () => {
//     player.togglePlay();
//   };

//   const goToNextSong = () => {
//     props.nextSong();
//   };

//   const goToPreviousSong = () => {
//     props.previousSong();
//   };

//   let trackName = currentTrack
//     ? currentTrack.track_window.current_track.name
//     : "";

//   let artistName = currentTrack
//     ? currentTrack.track_window.current_track.artists
//         .map((artist) => artist.name)
//         .join(", ")
//     : "";
//   let albumArt = currentTrack
//     ? currentTrack.track_window.current_track.album.images[0].url
//     : "../static/images/turntable.jpeg";

//   return (
//     <div>
//       <img
//         className="album"
//         src={albumArt}
//         height="400"
//         width="460"
//         alt="Album Art"
//       ></img>
//       <button id="button_bw" className="btn" onClick={goToPreviousSong}>
//         <i className="fa fa-backward"></i>
//       </button>
//       <button id="button_play" className="btn" onClick={togglePlay}>
//         <i className="fa fa-play"></i>
//       </button>
//       <button id="button_fw" className="btn" onClick={goToNextSong}>
//         <i className="fa fa-forward"></i>
//       </button>
//       <p className="track"> Track: {trackName} </p>
//       <p className="artists"> Artist(s): {artistName} </p>
//     </div>
//   );
// };

// export default Player;
