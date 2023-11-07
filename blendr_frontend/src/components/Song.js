import React from "react";
import "./Playlist.css";

const Song = (props) => {
  const selectSong = (e) => {
    props.updateTrack(e.target.value);
  };

  return (
    <li>
      <button
        className="btn btn-secondary song"
        value={props.uri}
        onClick={selectSong}
      >
        {props.track}
      </button>
    </li>
  );
};

export default Song;
