import React from "react";
import "./Login.css";
import icon from "../assets/sound.png";
import Button from "react-bootstrap/Button";

function Login() {
  const getSpotifyUserLogin = () => {
    fetch("http://localhost:8080/api/login")
      .then((response) => response.text())
      .then((response) => {
        window.location.replace(response);
      });
  };

  return (
    <div className="login">
      <h1 className="h1-title no-wrap">BeatBlendr</h1>
      <h2 className="h2-title">Every Mood Has a Beat – Discover Yours</h2>

      <h4 className="h4-title">Connect with Spotify to get started</h4>
      <img className="icon" src={icon} alt="Sound icon" />
      <Button
        className="button"
        variant="primary"
        onClick={getSpotifyUserLogin}
      >
        Login In with Spotify
      </Button>
      <img className="icon-2" src={icon} alt="Sound icon" />
      {/* <p className="">BeatBlendr does not save any personal </p> */}
      <p className="copyright">© 2023 BeatBlendr</p>
      <p className="made-by">made by BeatBlendr</p>
    </div>
  );
}
export default Login;
