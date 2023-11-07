import React from "react";
import Song from "./Song.js";
import "./Playlist.css";
const Playlist = (props) => {
  const songNames = props.tracks.map((track) => (
    <Song
      key={track.uri}
      updateTrack={props.updateTrack}
      songPlaying={props.trackPlaying}
      uri={track.uri}
      track={track.name}
    />
  ));

  return (
    <div>
      <ul className="playlist-panel">
        <span>{songNames}</span>
      </ul>
    </div>
  );
};

export default Playlist;
