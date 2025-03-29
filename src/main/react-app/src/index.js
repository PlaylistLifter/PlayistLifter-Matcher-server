import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));

fetch("http://localhost:5001")
    .then((response)=>{
        if(!response.ok){
            throw new Error("Network response was not ok");
        }
        return response.text();

    })
    .then((data)=>console.log(data))
    .catch((error)=> console.error("Error fetching data:",error))


root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
