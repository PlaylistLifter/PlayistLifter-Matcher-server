// src/pages/Callback.js
import React, { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";

function Callback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const code = searchParams.get("code");

    if (code) {
      // 백엔드에 code 전달하여 access token 발급 요청
      axios
        .get(`/api/spotify/callback?code=${code}`)
        .then((res) => {
          console.log("로그인 성공:", res.data);
          alert("✅ Spotify 로그인 완료!");
          navigate("/"); // 홈으로 리디렉션
        })
        .catch((err) => {
          console.error("콜백 처리 오류:", err);
          alert("❌ 로그인 처리 중 오류가 발생했습니다.");
          navigate("/"); // 실패해도 홈으로 이동
        });
    } else {
      alert("❌ 인증 코드가 없습니다.");
      navigate("/");
    }
  }, [searchParams, navigate]);

  return (
    <div style={{ textAlign: "center", paddingTop: "50px" }}>
      <h2>Spotify 로그인 처리 중...</h2>
    </div>
  );
}

export default Callback;
