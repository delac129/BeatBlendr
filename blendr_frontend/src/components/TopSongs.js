// import Box from "@mui/material/Box";
// import ScrollArrow from "./ScrollArrow";
import "./TopSongs.css";
import Header from "./Header";
import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

function TopSongs() {
  const [userId, setUserId] = useState("");
  let query = new URLSearchParams(useLocation().search);

  useEffect(() => {
    const id = query.get("userId");
    console.log(id);
    setUserId(id);
  }, [query]);

  const [userTopSongs, setUserTopSongs] = useState([]);

  useEffect(() => {
    if (userId) {
      console.log("Fetching user information" + userId);
      fetch("http://localhost:8080/api/user-saved-album?userId=" + userId)
        .then((res) => res.json())
        .then((data) => {
          console.log(data);
          setUserTopSongs(data);
        });
    }
  }, [userId]);

  return (
    <>
      {/* <Header />
      <div className="main">
        <ul className="songList">
          {userTopSongs ? (
            userTopSongs.map((song) => {
              return (
                <li key={song.name} className="songItem">
                  <img
                    className="songImacleage"
                    src={song.album.images[0].url}
                    alt={song.name}
                    loading="lazy"
                  />
                  <div className="songInfo">
                    <h4 className="songTitle">{song.name}</h4>
                    <h3 className="songArtist">{song.album.artist[0].name}</h3>
                  </div>
                </li>
              );
            })
          ) : (
            <h1>Loading...</h1>
          )}
        </ul>
      </div> */}
      <p>Check console</p>
    </>
  );
}

export default TopSongs;
