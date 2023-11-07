//import "./CreatePlaylistInSpotify.css";
import Header from "./Header";
import { useLocation } from "react-router-dom";
import { useEffect, useState } from "react";

function CreatePlaylistInSpotify(props) {
  const [userId, setUserId] = useState("");
  const [userPlaylistId, setUserPlaylistId] = useState("");
  const [moodValue, setMoodValue] = useState("");
  const [clicked, setClicked] = useState(false);

  let query = new URLSearchParams(useLocation().search);

  useEffect(() => {
    const id = query.get("userId");
    const val = query.get("mood");
    console.log(id);
    console.log(val);
    setUserId(id);
    setMood(val);
  }, [query]);

  useEffect(() => {
    if (userId && moodValue) {
      // Check both userId and moodValue
      console.log("Fetching user information" + userId);
      fetch(
        "http://localhost:8080/api/create-playlist?userId=" +
          userId +
          "&mood=" +
          moodValue
      )
        .then((res) => res.json())
        .then((data) => {
          console.log(data);
          setUserPlaylistId(data.playlistId);
        })
        .catch((error) => {
          console.error("Error fetching the access token:", error);
        });
    }
  }, [userId, moodValue]);

  function setMood(mood) {
    setMoodValue(mood);
    setClicked(true);
  }

  return (
    <>
      <Header />
      <div className="playlist-link">
        <h3 className="playlist-name">{userId}'s playlist</h3>
        <h5></h5>

        {userPlaylistId ? (
          <iframe
            className="iframe-player"
            src={`https://open.spotify.com/embed/playlist/${userPlaylistId}`}
            width="500"
            height="500"
            frameborder="0"
            allowtransparency="true"
            allow="encrypted-media"
          ></iframe>
        ) : (
          <p>Loading playlist...</p>
        )}
      </div>
    </>
  );
}

export default CreatePlaylistInSpotify;
