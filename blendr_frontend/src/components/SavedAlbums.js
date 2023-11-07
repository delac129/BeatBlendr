import "./SavedAlbums.css";
import Header from "./Header";
import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

function SavedAlbums() {
  const [userId, setUserId] = useState("");
  let query = new URLSearchParams(useLocation().search);

  useEffect(() => {
    const id = query.get("userId");
    console.log(id);
    setUserId(id);
  }, [query]);

  const [userSavedAlbums, setSavedAlbums] = useState([]);

  useEffect(() => {
    if (userId) {
      console.log("Fetching user information" + userId);
      fetch("http://localhost:8080/api/user-saved-album?userId=" + userId)
        .then((res) => res.json())
        .then((data) => {
          console.log(data);
          setSavedAlbums(data);
        });
    }
  }, [userId]);

  return (
    <>
      <Header />
      <div className="main">
        <ul className="album-list">
          {userSavedAlbums ? (
            userSavedAlbums.map((album) => {
              return (
                <li key={album.name} className="album-item">
                  <img
                    className="album-image"
                    src={album.album.images[0].url}
                    alt={album.name}
                    loading="lazy"
                  />
                  <div className="album-info">
                    <h4 className="album-title">{album.name}</h4>
                    <h3 className="album-artist">
                      {album.album.artists[0].name}
                    </h3>
                  </div>
                </li>
              );
            })
          ) : (
            <h1>Loading...</h1>
          )}
        </ul>
      </div>
    </>
  );
}

export default SavedAlbums;
