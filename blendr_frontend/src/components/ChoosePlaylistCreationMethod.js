import React, { useState, useEffect } from "react";
import Header from "./Header";
import { useLocation } from "react-router-dom";
import { Link } from "react-router-dom";
import "./ChoosePlaylistCreationMethod.css";
import icon from "../assets/sound.png";

function ChoosePlaylistCreationMethod() {
  const [userId, setUserId] = useState("");
  const [moodValue, setMoodValue] = useState(null);
  const [chosenMethod, setChosenMethod] = useState(null); // 'spotify' or 'browser'
  let query = new URLSearchParams(useLocation().search);

  useEffect(() => {
    const id = query.get("id");
    setUserId(id);
  }, [query]);

  function handleMoodSelection(mood) {
    setMoodValue(mood);
  }

  function handleMethodSelection(method) {
    setChosenMethod(method);
  }

  return (
    <>
      <Header userId={userId} />
      <div>
        {/* Display mood buttons only if moodValue is not set */}
        {!moodValue && (
          <div className="moodButtons">
            <h2 className="h2-title title">Your Mood, Your Music, Your Mix.</h2>
            <h4 className="h2-title title">
              Pick a mood, and we'll do the rest
            </h4>
            <button
              className="mood-btn very-sad"
              onClick={() => handleMoodSelection(0.05)}
            >
              Very Sad
            </button>
            <button
              className="mood-btn sad"
              onClick={() => handleMoodSelection(0.175)}
            >
              Sad
            </button>
            <button
              className="mood-btn somber"
              onClick={() => handleMoodSelection(0.375)}
            >
              Somber/Melancholy
            </button>
            <button
              className="mood-btn content"
              onClick={() => handleMoodSelection(0.625)}
            >
              Content/Neutral
            </button>
            <button
              className="mood-btn happy"
              onClick={() => handleMoodSelection(0.825)}
            >
              Happy/Excited
            </button>
            <button
              className="mood-btn elated"
              onClick={() => handleMoodSelection(0.95)}
            >
              Euphoric/Elated
            </button>

            <p className="p-title">
              BeatBlendr does{" "}
              <span className="not"> not save any personal data.</span>
            </p>
          </div>
        )}

        {/* Once the mood is set, display method selection */}
        {moodValue && !chosenMethod && (
          <div className="methodButtons">
            <h4 className="h2-title title">
              Your Personalized Playlist is Just a Click Away
            </h4>
            <button className="create-btn">
              <Link
                to={`/create-playlist-in-spotify?userId=${userId}&mood=${moodValue}`}
                className="button-link"
              >
                Create Playlist in Spotify
              </Link>
            </button>
          </div>
        )}

        {/* Conditionally render the chosen component
        {chosenMethod === "spotify" && (
          <CreatePlaylistInSpotify userId={userId} mood={moodValue} />
        )}
        {chosenMethod === "browser" && (
          <CreatePlaylistInBrowser mood={moodValue} user={userId} />
        )} */}
        <img className="icon-2-playlist-method" src={icon} alt="Sound icon" />
        <p className="copyright-playlist-method">© 2023 BeatBlendr</p>
        <p className="made-by-playlist-method">made by BeatBlendr</p>
      </div>
    </>
  );
}

export default ChoosePlaylistCreationMethod;
