import Login from "./components/Login";
import Home from "./components/Home";
import TopSongs from "./components/TopSongs";
import SavedAlbums from "./components/SavedAlbums";
import TopArtists from "./components/TopArtists";
import CreatePlaylistInSpotify from "./components/CreatePlaylistInSpotify";
import CreatePlaylistInBrowser from "./components/CreatePlayistInBrowser";
import ChoosePlaylistCreationMethod from "./components/ChoosePlaylistCreationMethod";
import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/home" element={<Home />} />
          <Route path="/top-tracks" element={<TopSongs />} />
          <Route path="/top-artists" element={<TopArtists />} />
          <Route path="/saved-albums" element={<SavedAlbums />} />
          <Route
            path="/create-playlist-method"
            element={<ChoosePlaylistCreationMethod />}
          />
          <Route
            path="/create-playlist-in-spotify"
            element={<CreatePlaylistInSpotify />}
          />
          <Route
            path="/create-playlist-in-browser"
            element={<CreatePlaylistInBrowser />}
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
