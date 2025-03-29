import "./style.css"; // 스타일 불러오기
import React from "react";

function InputBox({ value, onChange,onKeyDown }) {
  return (
    <input
      type="text"
      className="search-box" // CSS에서 스타일 적용
      placeholder="유튜브 URL을 입력하세요"
      value={value}
      onChange={onChange}
      onKeyDown={onKeyDown}
    />
  );
}

export default InputBox;

