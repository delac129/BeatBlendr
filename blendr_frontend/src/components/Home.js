import React from "react";
import Header from "./Header";
import { useLocation } from "react-router-dom";

function Home() {
  let query = new URLSearchParams(useLocation().search);
  let userId = query.get("id");
  let username = query.get("username");

  return (
    <>
      <Header userId={userId} />
      <div>
        {userId && username ? (
          <p>
            Welcome, {username} with ID: {userId}!
          </p>
        ) : (
          <p>There was an error fetching your user details.</p>
        )}
      </div>
    </>
  );
}

export default Home;
