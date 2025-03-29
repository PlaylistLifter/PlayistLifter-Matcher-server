import "./style.css";
import React from "react";

function Button({ text, onClick }) {
  return (
    <button className="search-button" onClick={onClick}>
      GO
    </button>
  );
}

export default Button;
