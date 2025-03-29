import { useState, useEffect } from "react";
import "./FloatingImage.css"; // 스타일 파일 불러오기
import React from "react";

// 이미지 목록 (여기에 원하는 이미지 URL 추가)
const astronaut2="/astronaut2.png";

const imageList = [astronaut2,astronaut2,astronaut2];

function FloatingImages() {
    const [images, setImages] = useState([]);

    useEffect(() => {
        // 랜덤한 이동 값을 생성하는 함수
        const generateRandomImageData = () => {
            return imageList.map((src, index) => ({
                id: index,
                src,
                top: `${Math.random() * 80 + 10}%`,
                left: `${Math.random() * 80 + 10}%`,
                size: `100px`,
                animationDuration: `${Math.random() * 6 + 4}s`, // 4~10초 랜덤 속도
                moveX: `${Math.random() * 1000 - 800}px`, // -200px ~ 200px 범위
                moveY: `${Math.random() * 1000 - 800}px`,
                rotate: `${Math.random() * 360}deg`, // 랜덤 회전
            }));
        };

        // 초기 이미지 데이터 설정
        setImages(generateRandomImageData());

        // 주기적으로 이동 방향을 갱신
        const interval = setInterval(() => {
            setImages(generateRandomImageData());
        }, 5000); // 5초마다 새로운 방향으로 이동

        return () => clearInterval(interval); // 컴포넌트가 사라질 때 인터벌 정리
    }, []);

    return (
        <div className="floating-container">
            {images.map((img) => (
                <img
                    key={img.id}
                    src={img.src}
                    className="floating-image"
                    style={{
                        top: img.top,
                        left: img.left,
                        width: img.size,
                        height: img.size,
                        animationDuration: img.animationDuration,
                        "--moveX": img.moveX,
                        "--moveY": img.moveY,
                        "--rotateAngle": img.rotate,
                    }}
                    alt="Floating Astronaut"
                />
            ))}
        </div>
    );
}

export default FloatingImages;

