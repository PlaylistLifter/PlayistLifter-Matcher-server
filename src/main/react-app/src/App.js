import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import FloatingImage from "./components/FloatingImage";
import InputBox from "./components/InputBox";
import "./components/style.css";
import Button from "./components/Button";
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

      const handleLogout = async () => {
        try {
          // 1. 서버에 저장된 토큰 제거
          await fetch('/api/logout');

          // 2. Spotify 로그아웃 페이지 잠깐 열고 닫기
          const popup = window.open(
            'https://accounts.spotify.com/logout',
            '_blank'
//            'width=1,height=1,left=-1000,top=-1000'
          );

          setTimeout(() => {
            if (popup) popup.close();
            // 3. 홈으로 리디렉션
            window.location.href = '/';
          }, 1000);
        } catch (error) {
          console.error("로그아웃 실패:", error);
        }
      };

      const handleChangeAccount = async () => {
        try {
          // 1. 내부 토큰 초기화 요청 (백엔드에 요청)
          await fetch('/api/change-account');

          // 2. Spotify 로그아웃 새 창으로 열기
          const popup = window.open(
            'https://accounts.spotify.com/logout',
            '_blank'
//            'width=1,height=1,left=-1000,top=-1000'
          );

          setTimeout(() => {
            if (popup) popup.close();
            // 3. 로그인 페이지로 이동
            window.location.href = "http://localhost:8080/login";
          }, 1000);
        } catch (error) {
          console.error("계정 변경 실패:", error);
        }
      };
  // 0. 로그인 상태 확인
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

  // 1. 유튜브 링크 전송 및 노래 추출
  const sendYouTubeLink = async () => {
    if (!youtubeLink) {
      alert("유튜브 링크를 입력하세요!");
      return;
    }
    setScreen("loading");

    try {
      const response = await axios.post("/link/send-link", { youtubeUrl: youtubeLink });
      if (response.data.songs) {
        setSongs(response.data.songs);
        setSelectedSongs(response.data.songs.map(song => song.id));
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

  const toggleSelectAll = () => {
    setSelectedSongs(selectedSongs.length === songs.length ? [] : songs.map(song => song.id));
  };

  const toggleSelection = (id) => {
    setSelectedSongs(prev =>
      prev.includes(id) ? prev.filter(songId => songId !== id) : [...prev, id]
    );
  };

  const matchSelectedSongs = async () => {
    if (selectedSongs.length === 0) {
      alert("최소한 하나의 노래를 선택하세요!");
      return;
    }

    try {
      await axios.get("/spotify/match-all");
      const matchedResponse = await axios.get("/api/matched/tracks");
      setMatchedTracks(matchedResponse.data);
      alert("✅ Spotify 매칭 완료!");
    } catch (error) {
      console.error("매칭 실패:", error);
      alert("❌ Spotify 매칭 중 오류가 발생했습니다.");
    }

    setSelectedSongs([]);
  };

  const saveSelectedSongs = async () => {
    if (selectedSongs.length === 0) {
      alert("저장할 노래가 없습니다. 최소 한 곡 이상 선택해주세요!");
      return;
    }

    const selected = songs.filter(song => selectedSongs.includes(song.id));
    const payload = { title: playlistTitle, songs: selected };

    try {
      const response = await axios.post("/api/playlist/add", payload);
      console.log("플레이리스트 저장 완료:", response.data);
      alert("✅ 선택한 노래들을 서버에 저장했습니다!");
      setIsSaved(true);
    } catch (error) {
      console.error("플레이리스트 저장 실패:", error);
      alert("❌ 서버 저장 중 오류 발생");
    }
  };

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
        <button className="change-account-button" onClick={handleChangeAccount}>
          계정 변경하기
        </button>
      </div>
    );
  };

  const clearRepositories = async () => {
    try {
      await axios.post("/api/playlist/clear");
      await axios.post("/api/matched/clear");
      alert("🧹 레포지터리를 초기화했습니다!");
      setSongs([]);
      setSelectedSongs([]);
      setMatchedTracks([]);
      setIsSaved(false);
      setScreen("home");
    } catch (error) {
      console.error("초기화 실패:", error);
      alert("❌ 초기화 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="app-container">
      <div className="auth-section">{renderAuthSection()}</div>

      {screen === "home" && (
        <>
          <div className="search-wrapper">
            <img className="sub-logo" src="/Logo2.png" alt="서브로고" />
            <div className="search-container">
              <InputBox
                value={youtubeLink}
                onChange={(e) => setYoutubeLink(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && sendYouTubeLink()}
              />
              <Button text="GO" onClick={sendYouTubeLink} />
            </div>
          </div>
          <img className="main-logo" src="/Logo.png" alt="메인로고" />
          <FloatingImage />
        </>
      )}

      {screen === "loading" && (
        <div className="loading-screen">
          <p className="loading-txt">Extracting...</p>
          <ScaleLoader className="custom-spinner" color="#ffffff" />
        </div>
      )}

      {screen === "result" && (
        <div className="result-screen">
          <div className="result-layout">
            <div className="left-panel">
              <h2>🎵 추출된 노래 목록</h2>
              <button className="select-all-button" onClick={toggleSelectAll}>
                {selectedSongs.length === songs.length ? "모두 해제" : "모두 체크"}
              </button>

              <div className="song-container">
                <div className="song-list">
                  {songs.length > 0 ? (
                    songs.map((song) => (
                      <div key={song.id} className="song-card">
                        <input
                          type="checkbox"
                          className="song-checkbox"
                          checked={selectedSongs.includes(song.id)}
                          onChange={() => toggleSelection(song.id)}
                        />
                        <p className="song-title">{song.title}</p>
                        <p className="song-artist">🎤 {song.artist}</p>
                      </div>
                    ))
                  ) : (
                    <p>노래 목록이 없습니다.</p>
                  )}
                </div>
              </div>

              <div className="button-row">
                {!isSaved && (
                  <button className="save-button" onClick={saveSelectedSongs}>
                    💾 저장
                  </button>
                )}
                {isSaved && matchedTracks.length === 0 && (
                  <button className="match-button" onClick={matchSelectedSongs}>
                    ✅ 매칭
                  </button>
                )}
                <button className="clear-button" onClick={clearRepositories}>
                  🧹 초기화
                </button>
                <button className="back-button" onClick={() => setScreen("home")}>
                  🏠 홈으로
                </button>
              </div>
            </div>

            <div className="right-panel">
              {matchedTracks.length > 0 && (
                <div className="match-results">
                  <h3>🎯 매칭 결과</h3>
                  <div className="match-scroll-box">
                    <ul className="match-list">
                      {matchedTracks.map((match, idx) => {
                        const isMatched = match.spotifyTrackId && match.spotifyTrackId.trim() !== "";
                        return (
                          <li key={idx} className="match-item">
                            <div className="match-info">
                              <strong>{match.originalArtist} - {match.originalTitle}</strong>
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
                    🎧 Spotify 재생목록 만들기
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
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
