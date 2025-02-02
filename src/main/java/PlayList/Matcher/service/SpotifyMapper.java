package PlayList.Matcher.service;

import PlayList.Matcher.dto.SearchResponseDto;
import org.springframework.stereotype.Component;

@Component
public class SpotifyMapper {
    public SearchResponseDto toSearchDto(String artistName, String title, String albumName, String imageUrl) {
        return SearchResponseDto.builder()
                .artistName(artistName)
                .title(title)
                .albumName(albumName)
                .imageUrl(imageUrl)
                .build();

    }
}
