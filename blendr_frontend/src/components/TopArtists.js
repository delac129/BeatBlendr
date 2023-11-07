import { useEffect, useState } from "react";
import "./TopArtists.css";
import Header from "./Header";
import { useLocation } from "react-router-dom";

const TopArtists = () => {
  const [userId, setUserId] = useState("");
  let query = new URLSearchParams(useLocation().search);

  useEffect(() => {
    const id = query.get("userId");
    console.log(id);
    setUserId(id);
  }, [query]);

  useEffect(() => {
    if (userId) {
      console.log("Fetching user information" + userId);
      fetch("http://localhost:8080/api/aggregate-top-tracks?userId=" + userId)
        .then((res) => res.json())
        .then((data) => {
          console.log(data);
          setUserTopArtists(data);
        });
    }
  }, [userId]);

  const [userTopArtists, setUserTopArtists] = useState([]);
  return (
    <>
      <Header />
      {/* <div className="user-following">
        {userTopArtists && userTopArtists.length > 0 ? (
          userTopArtists.map((artist) => <h3 key={artist}>{artist}</h3>)
        ) : (
          <h3>failed to load</h3>
        )}
      </div> */}
    </>
  );
};
export default TopArtists;
