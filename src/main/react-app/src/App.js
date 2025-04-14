import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import FloatingImage from "./components/FloatingImage";
import "./components/style.css";
import { ScaleLoader } from "react-spinners";
import axios from "axios";
import Callback from "./pages/Callback";

function MainApp() {
  const [youtubeLink, setYoutubeLink] = useState("");
  const [songs, setSongs] = useState([]);
  const [screen, setScreen] = useState("home");
  const [selectedSongs, setSelectedSongs] = useState([]);
  const [userInfo, setUserInfo] = useState(null);
  const [matchedTracks, setMatchedTracks] = useState([]);
  const [isSaved, setIsSaved] = useState(false);
  const [playlistTitle, setPlaylistTitle] = useState("");
  const [isMatched, setIsMatched] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
//0.로그인 로그아웃
  const handleLogout = async () => {
    try {
      await fetch('/api/logout');
      const popup = window.open('https://accounts.spotify.com/logout', '_blank');
      setTimeout(() => {
        if (popup) popup.close();
        window.location.href = '/';
      }, 1000);
    } catch (error) {
      console.error("로그아웃 실패:", error);
    }
  };

  const handleChangeAccount = async () => {
    try {
      await fetch('/api/change-account');
      const popup = window.open('https://accounts.spotify.com/logout', '_blank');
      setTimeout(() => {
        if (popup) popup.close();
        window.location.href = "http://localhost:8080/login";
      }, 1000);
    } catch (error) {
      console.error("계정 변경 실패:", error);
    }
  };

  useEffect(() => {
    const checkLoginStatus = async () => {
      try {
        const response = await axios.get("/spotify/me");
        setUserInfo(response.data);
      } catch (error) {
        setUserInfo(null);
      }
    };
    checkLoginStatus();
  }, []);

  const renderAuthSection = () => {
    if (!userInfo) {
      return (
          <button className="login-button" onClick={() => window.location.href = "http://localhost:8080/login"}>
            로그인
          </button>
      );
    }
    return (
        <div className="user-info">
          <p>안녕하세요, <strong>{userInfo.displayName || userInfo.id}</strong>님!</p>
          <p><small>이메일: {userInfo.email || '정보 없음'}</small></p>
          <button className="logout-button" onClick={handleLogout}>로그아웃</button>
          <button className="change-account-button" onClick={handleChangeAccount}>계정 변경하기</button>
        </div>
    );
  };

//1. 유튜브 링크 전송
  const sendYouTubeLink = async () => {
    if (!youtubeLink) {
      alert("유튜브 링크를 입력하세요!");
      return;
    }
    setScreen("loading");
    try {
      const response = await axios.post("/link/send-link", { youtubeUrl: youtubeLink });
      if (response.data.songs) {
        const songsWithId = response.data.songs.map((song, index) => ({
          id: song.id ?? `temp-${index}`,
          title: song.title,
          artist: song.artist
        }));
        setSongs(songsWithId);
        setSelectedSongs(songsWithId.map(song => String(song.id)));
        setPlaylistTitle(response.data.title);
        setIsSaved(false);
        setScreen("result");
      } else {
        throw new Error("노래 데이터가 없습니다.");
      }
    } catch (error) {
      console.error("Error fetching data:", error);
      alert("❌ 서버에서 데이터를 불러오는 데 실패했습니다.");
      setScreen("home");
    }
  };
  //2. 노래추출화면 구현
  //노래 전체선택
  const toggleSelectAll = () => {
    const allSelected = selectedSongs.length === songs.length;
    setSelectedSongs(allSelected ? [] : songs.map(song => String(song.id)));
  };

  const toggleSelection = (id) => {
    const stringId = String(id);
    setSelectedSongs(prev =>
        prev.includes(stringId) ? prev.filter(songId => songId !== stringId) : [...prev, stringId]
    );
  };
  //선택된 노래 저장
  const saveSelectedSongs = async () => {
    const selected = songs.filter(song => selectedSongs.includes(String(song.id)));
    if (selected.length === 0) {
      alert("저장할 노래가 없습니다. 최소 한 곡 이상 선택해주세요!");
      return;
    }

    const payload = { title
          : playlistTitle, songs: selected };
    try {
      setIsProcessing(true);
      await axios.post("/api/playlist/add", payload);
      setIsSaved(true);

      await axios.get("/spotify/match-all");
      const matched = await axios.get("/api/matched/tracks");

      const completeMatchedList = selected.map((song) => {
        const foundMatch = matched.data.find(m => m.originalTitle === song.title && m.originalArtist === song.artist);
        return foundMatch || {
          originalTitle: song.title,
          originalArtist: song.artist,
          spotifyTrackId: "",
          matchedArtist: "",
          matchedTitle: ""
        };
      });

      setMatchedTracks(completeMatchedList);
      setIsMatched(true);
      setSongs([]);
      setSelectedSongs([]);
    } catch (error) {
      console.error("저장 또는 매칭 실패:", error);
      alert("❌ 저장 또는 매칭 중 오류가 발생했습니다.");
    } finally {
      setIsProcessing(false);
    }
  };
  //3. 매칭파트
  //스포티파이 플레이리스트 생성
  const createSpotifyPlaylist = async () => {
    try {
      const response = await axios.get("/spotify/create-playlist2");
      const playlistUrl = response.data;
      alert("🎉 Spotify 재생목록이 생성되었습니다!");
      window.open(playlistUrl, "_blank");
    } catch (error) {
      console.error("재생목록 생성 실패:", error);
      alert("❌ 재생목록 생성 실패");
    }
  };

  //4. 레포지토리 초기화
  const clearRepositories = async () => {
    try {
      await axios.post("/api/playlist/clear");
      await axios.post("/api/matched/clear");
      setSongs([]);
      setSelectedSongs([]);
      setMatchedTracks([]);
      setIsSaved(false);
      setIsMatched(false);
      setScreen("home");
    } catch (error) {
      console.error("초기화 실패:", error);
      alert("❌ 초기화 중 오류가 발생했습니다.");
    }
  };

  return (//렌더링
      <div className="app-container">
        <FloatingImage/>
        {isProcessing ? (//프로세싱중이라면 로딩화면
            <div className="loading-screen">
              <p className="loading-txt">Matching...</p>
              <ScaleLoader className="custom-spinner" color="#ffffff" />
            </div>
        ) : (//프로세싱중이 아니라면 렌더링 화면
            <>
              <div className="auth-section">{renderAuthSection()}</div>
              {screen === "home" && (//screen이 홈페이지로 셋 됐을때
                  <>
                    <div className="search-wrapper">
                      <text className="logo">PLAYLIST_LIFTER_</text>

                      <div className="search-container">
                        <input
                            type="text"
                            placeholder="유튜브 URL을 입력하세요"
                            className="search-box"
                            value={youtubeLink}
                            onChange={(e) => setYoutubeLink(e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === "Enter") {
                                e.preventDefault();
                                sendYouTubeLink();
                              }}}
                        />
                        <button className="search-button" onClick={sendYouTubeLink}>Go</button>
                      </div>
                    </div>
                  </>
              )}

              {screen === "loading" && (//screen이 로딩화면일때
                  <div className="loading-screen">
                    <p className="loading-txt">Extracting...</p>
                    <ScaleLoader className="custom-spinner" color="#ffffff" />
                  </div>
              )}

              {screen === "result" && (//screen 결과화면
                  <div className="result-screen">
              <span
                  className="result-screen-logo-button"
                  onClick={clearRepositories}
              >PLAYLIST_LIFTER_</span>
                    <div className="center-panel">
                      {!isMatched && songs.length > 0 && (
                          <>


                            <div className="song-container-wrapper">
                              <h2 className="h2">Songs from Youtube</h2>
                              <div className="song-container">
                                <div className="song-list">
                                  {songs.map((song, index) => (
                                      <div key={song.id || index} className="song-card">
                                        <input
                                            type="checkbox"
                                            className="song-checkbox"
                                            checked={selectedSongs.includes(String(song.id))}
                                            onChange={() => toggleSelection(song.id)}
                                        />
                                        <p className="song-title">{song.title}</p>
                                        <p className="song-artist">🎤 {song.artist}</p>
                                      </div>
                                  ))}
                                </div>
                              </div>
                              <button className="select-all-button" onClick={toggleSelectAll}>
                                {selectedSongs.length === songs.length ? "all ✓" : "all"}
                              </button>
                            </div>
                          </>
                      )}
                      <div className="button-row">
                        {!isSaved && songs.length > 0 && (
                            <button className="save-button" onClick={saveSelectedSongs}>
                              Start Matching
                            </button>
                        )}
                      </div>
                      {matchedTracks.length > 0 && (
                          <div className="match-results">
                            <h3 className="h3">Your Song List</h3>
                            <div className="match-scroll-box">
                              <ul className="match-list">
                                {matchedTracks.map((match, idx) => {
                                  const isMatched = match.spotifyTrackId && match.spotifyTrackId.trim() !== "";
                                  return (
                                      <li key={idx} className="match-item">
                                        <div className="match-info">
                                          <strong>{match.originalTitle} - {match.originalArtist}</strong>
                                          <span className={isMatched ? "match-success" : "match-fail"}>
                                  {isMatched ? "✓ 매칭 성공" : "✗ 매칭 실패"}
                                </span>
                                          {isMatched && (
                                              <span className="match-details">
                                    Spotify: {match.matchedArtist} - {match.matchedTitle}
                                  </span>
                                          )}
                                        </div>
                                      </li>
                                  );
                                })}
                              </ul>
                            </div>
                            <button className="create-playlist-button" onClick={createSpotifyPlaylist}>
                              Create Spotify Playlist
                            </button>
                          </div>
                      )}
                    </div>
                  </div>
              )}
            </>
        )}
      </div>
  );
}

function App() {
  return (
      <Router>
        <Routes>
          <Route path="/" element={<MainApp />} />
          <Route path="/callback" element={<Callback />} />
        </Routes>
      </Router>
  );
}

export default App;
