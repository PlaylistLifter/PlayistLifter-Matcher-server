package PlayList.Matcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDto {
    private String artistName;
    private String title;
    private String albumName;
    private String imageUrl;

    // 추가 필드
    private String spotifyTrackId;   // Spotify 트랙 ID
    private String originalArtist;    // 원본(유튜브) 아티스트
    private String originalTitle;     // 원본(유튜브) 트랙 제목

    // 매칭된 아티스트와 제목 (UI 표시용)
    private String matchedArtist;
    private String matchedTitle;

    // 기존 생성자와 호환성을 위한 메소드
    public static class SearchResponseDtoBuilder {
        private String spotifyTrackId;
        private String originalArtist;
        private String originalTitle;
        private String matchedArtist;
        private String matchedTitle;

        public SearchResponseDtoBuilder spotifyTrackId(String spotifyTrackId) {
            this.spotifyTrackId = spotifyTrackId;
            return this;
        }

        public SearchResponseDtoBuilder originalArtist(String originalArtist) {
            this.originalArtist = originalArtist;
            return this;
        }

        public SearchResponseDtoBuilder originalTitle(String originalTitle) {
            this.originalTitle = originalTitle;
            return this;
        }

        public SearchResponseDtoBuilder matchedArtist(String matchedArtist) {
            this.matchedArtist = matchedArtist;
            return this;
        }

        public SearchResponseDtoBuilder matchedTitle(String matchedTitle) {
            this.matchedTitle = matchedTitle;
            return this;
        }
    }
}